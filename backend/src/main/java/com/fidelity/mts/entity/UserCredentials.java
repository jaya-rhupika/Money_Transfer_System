package com.fidelity.mts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_credentials")
public class UserCredentials {
    
    @Id
    @Column(name = "account_id")
    private long accountId;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Account account;
    
    public UserCredentials() {
    }
    
    public UserCredentials(long accountId, String username, String password) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
    }
    
    public long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
}
