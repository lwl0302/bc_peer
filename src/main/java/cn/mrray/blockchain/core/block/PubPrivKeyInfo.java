package cn.mrray.blockchain.core.block;

import javax.persistence.*;

/**
 * 公私钥信息表
 */
@Entity
@Table(name = "t_pubprivkey_info")
public class PubPrivKeyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;//标识ID
    private String publicKey;//公钥
    private String privateKey;//私钥
    private String status;//状态
    private int type;//类型

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
