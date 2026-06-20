package com.fidelity.mts.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fidelity.mts.enums.AccountStatus;
import jakarta.persistence.*;

@Entity
@Table(name="accounts")
public class Account{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
	@Column(columnDefinition = "BIGINT",name = "id")
	private long id;

	@Column(name = "holder_name", columnDefinition = "VARCHAR(255)",nullable = false)
	private String holderName;
	@Column(precision = 18, scale = 2, nullable = false)
	private BigDecimal balance;
	@Column(columnDefinition = "VARCHAR(20)",nullable = false)
	@Enumerated(EnumType.STRING)
	private AccountStatus status;
	@Column(columnDefinition = "INT DEFAULT 0")
	private int version;

	@Column(name = "last_updated",columnDefinition = "TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime lastUpdated;
	@Column(name = "total_reward_points", columnDefinition = "INT DEFAULT 0")
	private int totalRewardPoints;
	public Account() {
	}

	public Account(long id, String holderName, double balance, AccountStatus status, double version,
				   LocalDateTime lastUpdated) {
		this.id = id;
		this.holderName = holderName;
		this.balance = BigDecimal.valueOf(balance);
		this.status = status;
		this.version = (int) version;
		this.lastUpdated = lastUpdated;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = (int) version;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public int getTotalRewardPoints() {
		return totalRewardPoints;
	}

	public void setTotalRewardPoints(int totalRewardPoints) {
		this.totalRewardPoints = totalRewardPoints;
	}

	public BigDecimal debit(BigDecimal current_bal, BigDecimal debit_amount) {
		return current_bal.subtract(debit_amount) ;
	}

	public BigDecimal credit(BigDecimal current_bal,BigDecimal credit_amount) {
		return current_bal.add(credit_amount);
	}

	public boolean isActive() {
		return status == AccountStatus.ACTIVE;
	}
}