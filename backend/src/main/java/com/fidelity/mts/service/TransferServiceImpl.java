package com.fidelity.mts.service;

import com.fidelity.mts.dto.TransferRequestDto;
import com.fidelity.mts.dto.TransferResponseDto;
import com.fidelity.mts.entity.Account;
import com.fidelity.mts.entity.TransactionLog;
import com.fidelity.mts.enums.TransactionStatus;
import com.fidelity.mts.exceptions.*;
import com.fidelity.mts.repository.AccountRepository;
import com.fidelity.mts.repository.TransactionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TransactionLogRepository transactionLogRepository;

    @Override
    public TransferResponseDto transfer(TransferRequestDto transferRequest) {
        long fromId=transferRequest.getFromAccountId();
        Account fromAccount=accountService.getAccount(fromId);

        long toId=transferRequest.getToAccountId();
        Account toAccount=accountService.getAccount(toId);

        BigDecimal amount= transferRequest.getAmount();
        String idempotency_key = transferRequest.getIdempotencyKey();

        boolean validTransfer=validateTransfer(fromAccount,toAccount,amount,idempotency_key);
        if(validTransfer){
            return executeTransfer(fromAccount, toAccount, amount, transferRequest.getIdempotencyKey());
        }
        else{
            return null;
        }


    }

    @Override
    public boolean validateTransfer(Account senderAcc, Account recieverAcc, BigDecimal amountToBeDebited, String idempotency_key) {

        //Checking Self Transfer
        if(senderAcc.getId()==(recieverAcc.getId())){
            UUID temp = UUID.randomUUID();
            TransactionLog transactionLog = new TransactionLog(senderAcc.getId(), recieverAcc.getId(), amountToBeDebited.doubleValue(),TransactionStatus.FAILURE,"Self transfer is not allowed",idempotency_key, LocalDateTime.now());
            transactionLog.setId(temp);
            transactionLogRepository.save(transactionLog);
            throw new SelfTransferException("Self Transfer is not allowed!");

        }

        //Checking if Sender Account is active
        if(!senderAcc.isActive()){
            UUID temp = UUID.randomUUID();
            TransactionLog transactionLog = new TransactionLog(senderAcc.getId(), recieverAcc.getId(), amountToBeDebited.doubleValue(),TransactionStatus.FAILURE,"Sender Account Not Active !",idempotency_key, LocalDateTime.now());
            transactionLog.setId(temp);
            transactionLogRepository.save(transactionLog);
            throw new AccountNotActiveException("Sender Account Not Active !");
        }

        //Checking if Receiver Account is active
        if(!recieverAcc.isActive()){
            UUID temp = UUID.randomUUID();
            TransactionLog transactionLog = new TransactionLog(senderAcc.getId(), recieverAcc.getId(), amountToBeDebited.doubleValue(),TransactionStatus.FAILURE,"Receiver Account Not Active !",idempotency_key, LocalDateTime.now());
            transactionLog.setId(temp);
            transactionLogRepository.save(transactionLog);
            throw new AccountNotActiveException("Receiver Account Not Active !");
        }

        //Checking if amount > 0

        if(amountToBeDebited.compareTo(BigDecimal.ZERO)<0){
            UUID temp = UUID.randomUUID();
            TransactionLog transactionLog = new TransactionLog(senderAcc.getId(), recieverAcc.getId(), amountToBeDebited.doubleValue(),TransactionStatus.FAILURE,"Negative Amount cannot be transferred!",idempotency_key, LocalDateTime.now());
            transactionLog.setId(temp);
            transactionLogRepository.save(transactionLog);
            throw new NegativeAmountException("Negative Amount cannot be transferred!");
        }

        if((amountToBeDebited.compareTo(senderAcc.getBalance())>0)){
            UUID temp = UUID.randomUUID();
            TransactionLog transactionLog = new TransactionLog(senderAcc.getId(), recieverAcc.getId(), amountToBeDebited.doubleValue(),TransactionStatus.FAILURE,"Balance is not sufficient",idempotency_key, LocalDateTime.now());
            transactionLog.setId(temp);
            transactionLogRepository.save(transactionLog);
            throw new InsufficientBalanceException("Balance is not sufficient");
        }

        return true;
    }

    @Override
    @Transactional
    public TransferResponseDto executeTransfer(Account senderAcc, Account recieverAcc, BigDecimal amountToBeDebited, String idempotency_key) {
        if(transactionLogRepository.findByIdempotencyKey(idempotency_key).size()!=0){
            throw new DuplicateTransferException("Duplicate Transaction not allowed!!");
        }

        senderAcc.setBalance(senderAcc.debit(senderAcc.getBalance(),amountToBeDebited));
        recieverAcc.setBalance(recieverAcc.credit(recieverAcc.getBalance(),amountToBeDebited));

        senderAcc.setLastUpdated(LocalDateTime.now());
        recieverAcc.setLastUpdated(LocalDateTime.now());

        accountRepository.saveAndFlush(senderAcc);
        accountRepository.saveAndFlush(recieverAcc);
        UUID temp = UUID.randomUUID();
        TransactionLog transactionLog = new TransactionLog(senderAcc.getId(), recieverAcc.getId(), amountToBeDebited.doubleValue(),TransactionStatus.SUCCESS,"NULL",idempotency_key, LocalDateTime.now());
        transactionLog.setId(temp);
        transactionLogRepository.save(transactionLog);
        // Reward points logic: eligible when transfer success, amount > 100, and not self transfer
        int points = 0;
        try {
            if (transactionLog.getStatus() == TransactionStatus.SUCCESS && senderAcc.getId() != recieverAcc.getId() && amountToBeDebited.compareTo(new BigDecimal("100")) > 0) {
                points = amountToBeDebited.divideToIntegralValue(new BigDecimal("100")).intValue();
            }
        } catch (Exception ex) {
            points = 0;
        }

        transactionLog.setPoints(points);
        transactionLogRepository.save(transactionLog);

        if (points > 0) {
            senderAcc.setTotalRewardPoints(senderAcc.getTotalRewardPoints() + points);
            accountRepository.saveAndFlush(senderAcc);
        }
        return new TransferResponseDto(
                "TRX-" + transactionLog.getId(),
                "Transfer completed",
                TransactionStatus.SUCCESS,
                transactionLog.getFromAccountId(),
                transactionLog.getToAccountId(),
                transactionLog.getAmount()
        );
    }
}
