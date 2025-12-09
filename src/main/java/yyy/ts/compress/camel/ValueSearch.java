package yyy.ts.compress.camel;
import fi.iki.yak.ts.compression.gorilla.Value;
import org.checkerframework.checker.units.qual.C;
import yyy.ts.compress.camel.CamelUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 值查询
 */
public class ValueSearch {
    public List<SkiplistNode> searchValue(BPlusDecimalTree bPlusDecimalTree, BPlusTree2 bPlusTree2, double value){
        BigDecimal big_value = BigDecimal.valueOf(value);
        BigDecimal decimal_value = big_value.subtract(BigDecimal.valueOf(big_value.intValue()));
        int decimal_count = CamelUtils.countDecimalPlaces(decimal_value);
//        byte[] intBytes = CamelUtils.compressInteger((int)value);
//        byte[] decimalBytes = CamelUtils.compressDecimal(decimal_count, decimal_value.intValue());

        // 小数部分的list
        List<SkiplistNode> decimalList = new ArrayList<>();
        Map<String, Object> decimalRes = CamelUtils.countXORedVal(decimal_count, decimal_value);
        byte[] XORed = (byte[]) decimalRes.get("XORed");
        BigDecimal m = (BigDecimal) decimalRes.get("m");
        int search_value = m.multiply(new BigDecimal(Math.pow(10, decimal_count))).intValue();
        KeyNode keyNode = bPlusDecimalTree.searchKeyNode(search_value);
        if (keyNode == null || keyNode.flagFalseNode == null) {
            return null;
        }
        if (XORed.length == 1 && XORed[0] == 0) {
            decimalList = keyNode.flagFalseNode.tsNodeList;
        } else {
            if (keyNode.flagTrueNode == null) {
                return null;
            }
            DecimalNode decimalNode = bPlusDecimalTree.searchDecimalNode(keyNode.flagTrueNode.decimalNodes, XORed);
            if (decimalNode == null) {
                return null;
            }
            decimalList = decimalNode.tsNodeList;
        }

        // 整数部分的list
        List<SkiplistNode> integerList = new ArrayList<>();
        int firstVal = 64;
        int diffVal = big_value.intValue()-firstVal;

        long start = System.nanoTime();
        IntKeyNode2 intKeyNode2 = bPlusTree2.searchKeyNode(bPlusTree2.getRoot(bPlusTree2), diffVal);
        if (intKeyNode2!=null && intKeyNode2.tsNodesList!=null) {
            integerList = intKeyNode2.tsNodesList;
        }

        // 找到两个List的交集
        // 使用Stream API找到交集
        List<SkiplistNode> result = new ArrayList<>();
        if (integerList !=null && decimalList != null) {
            for (SkiplistNode tsNode: integerList){
                for (SkiplistNode tsNode1: decimalList) {
                    if (tsNode.timeStamp == tsNode1.timeStamp) {
                        result.add(tsNode);
                    }
                }
            }
        }

//        long end = System.nanoTime();
//        System.out.println((end-start)/1000.0);
//        List<TSNode> intersection = integerList.stream()
//                .filter(decimalList::contains)
//                .collect(Collectors.toList());
        return result;
    }
    public static void main(String[] args) {

    }
}


