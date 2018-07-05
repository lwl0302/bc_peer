package cn.mrray.blockchain.core.util;

import cn.mrray.blockchain.grpc.common.Status;
import cn.mrray.blockchain.grpc.peer.ChaincodePeerReply;
import cn.mrray.blockchain.grpc.peer.SdkPeerReply;
import org.apache.commons.lang3.StringUtils;

/**
 * @author weijun
 * @date 2018/6/19 17:22
 */
public class ChaincodePeerReplyUtil {
    private ChaincodePeerReplyUtil() {
    }

    public static ChaincodePeerReply newSussessReply() {
        return newReply(Status.SUCCESS_VALUE, null, null);
    }

    public static ChaincodePeerReply newSussessReply(String message) {
        return newReply(Status.SUCCESS_VALUE, message, null);
    }

    public static ChaincodePeerReply newSussessReply(String message, String payload) {
        return newReply(Status.SUCCESS_VALUE, message, payload);
    }

    public static ChaincodePeerReply newErrorReply() {
        return newReply(Status.INTERNAL_SERVER_ERROR_VALUE, null, null);
    }

    public static ChaincodePeerReply newErrorReply(String message) {
        return newReply(Status.INTERNAL_SERVER_ERROR_VALUE, message, null);
    }

    public static ChaincodePeerReply newErrorReply(String message, String payload) {
        return newReply(Status.INTERNAL_SERVER_ERROR_VALUE, message, payload);
    }

    public static ChaincodePeerReply newReply(int code, String message, String payload) {
        ChaincodePeerReply.Builder builder = ChaincodePeerReply.newBuilder();
        if (StringUtils.isNoneBlank(message)) {
            builder.setMessage(message);
        }
        if (StringUtils.isNoneBlank(payload)) {
            builder.setPayload(payload);
        }
        return builder.setCode(code)
                .build();
    }

}
