package com.fidelity.mts.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fidelity.mts.enums.TransactionStatus;
import jakarta.persistence.*;

@Entity
@Table(name="transaction_logs")
public class TransactionLog {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "CHAR(36)")
	private UUID id;

	@Column(columnDefinition = "BIGINT", name="from_account_id", nullable = false)
	private long fromAccountId;

	@Column(columnDefinition = "BIGINT", name="to_account_id", nullable = false)
	private long toAccountId;

	@Column(precision = 18, scale = 2, nullable = false)
	private BigDecimal amount;

	@Column(name = "status", columnDefinition = "VARCHAR(20)", nullable = false)
	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	@Column(name = "failure_reason", columnDefinition = "VARCHAR(255)")
	private String failureReason;

	@Column(name = "idempotency_key", columnDefinition = "VARCHAR(100) UNIQUE")
	private String idempotencyKey;

	@Column(name = "created_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdOn;

	@Column(name = "points", columnDefinition = "INT DEFAULT 0")
	private int points;

	@ManyToOne
	@JoinColumn(
			name="from_account_id",
			referencedColumnName = "id",
			insertable = false,
			updatable = false
	)
	private Account FromAccount;

	@ManyToOne
	@JoinColumn(
			name="to_account_id",
			referencedColumnName = "id",
			insertable = false,
			updatable = false
	)
	private Account ToAccount;

	public TransactionLog() {

	}

	public TransactionLog(long fromAccountId, long toAccountId, double amount, TransactionStatus status,
						  String failureReason, String idempotencyKey, LocalDateTime createdOn) {

		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = BigDecimal.valueOf(amount);
		this.status = status;
		this.failureReason = failureReason;
		this.idempotencyKey = idempotencyKey;
		this.createdOn = createdOn;
		this.points = 0;
	}

	// Getters and Setters

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public long getFromAccountId() {
		return fromAccountId;
	}

	public void setFromAccountId(long fromAccountId) {
		this.fromAccountId = fromAccountId;
	}

	public long getToAccountId() {
		return toAccountId;
	}

	public void setToAccountId(long toAccountId) {
		this.toAccountId = toAccountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = BigDecimal.valueOf(amount);
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
}
