package cn.mrray.blockchain.core.util;

import cn.mrray.blockchain.core.block.AllInfo;

public class AllInfoUtil {

    private static AllInfo allInfo = new AllInfo();

    private AllInfoUtil() {

    }

    public static void initAllInfo(AllInfo allInfoTemp) {
        if (allInfoTemp == null) {
            allInfo = new AllInfo();
            return;
        }
        allInfo = allInfoTemp;
    }

    public static AllInfo getAllInfo() {
        return allInfo;
    }
}
