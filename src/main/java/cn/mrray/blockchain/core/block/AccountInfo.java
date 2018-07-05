package cn.mrray.blockchain.core.block;

import javax.persistence.*;

/**
 * 用户信息
 */
@Entity
@Table(name = "t_account_info")
public class AccountInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int accountId;//用户ID
    private String accountName;//用户名称
    private String password;//用户密码
    @OneToOne
    @JoinColumn(name = "id",foreignKey = @ForeignKey(name = "fk_pubprivkey_fc"))
    private PubPrivKeyInfo pubPrivKeyInfo;//公私钥信息

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public PubPrivKeyInfo getPubPrivKeyInfo() {
        return pubPrivKeyInfo;
    }

    public void setPubPrivKeyInfo(PubPrivKeyInfo pubPrivKeyInfo) {
        this.pubPrivKeyInfo = pubPrivKeyInfo;
    }
}
