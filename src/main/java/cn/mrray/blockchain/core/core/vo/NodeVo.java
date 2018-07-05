package cn.mrray.blockchain.core.core.vo;

import java.io.Serializable;

/**
 * @author weijun
 * @date 2018/6/11 21:55
 */
public class NodeVo implements Serializable {
    private String name;
    private String sign;

    public String getName() {
        return name;
    }

    public NodeVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public NodeVo setSign(String sign) {
        this.sign = sign;
        return this;
    }
}
