package yyy.ts.compress.camel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 范围查询
 */
public class RangeSearch {
    public List<SkiplistNode> searchRangeValue(BPlusTree bPlusTree, double begin, double end){
        BigDecimal begin_value = BigDecimal.valueOf(begin);
        BigDecimal begin_decimal_value = begin_value.subtract(BigDecimal.valueOf(begin_value.intValue()));
        int begin_decimal_count = CamelUtils.countDecimalPlaces(begin_decimal_value);
        Map<String, Object> begin_decimalRes = CamelUtils.countXORedVal(begin_decimal_count, begin_decimal_value);
        byte[] begin_XORed = (byte[]) begin_decimalRes.get("XORed");
        BigDecimal begin_m = (BigDecimal) begin_decimalRes.get("m");
        int begin_search_value = begin_m.multiply(new BigDecimal(Math.pow(10, begin_decimal_count))).intValue();

        BigDecimal end_value = BigDecimal.valueOf(end);
        BigDecimal end_decimal_value = end_value.subtract(BigDecimal.valueOf(end_value.intValue()));
        int end_decimal_count = CamelUtils.countDecimalPlaces(end_decimal_value);
        Map<String, Object> end_decimalRes = CamelUtils.countXORedVal(end_decimal_count, end_decimal_value);
        byte[] end_XORed = (byte[]) end_decimalRes.get("XORed");
        BigDecimal end_m = (BigDecimal) end_decimalRes.get("m");
        int end_search_value = end_m.multiply(new BigDecimal(Math.pow(10, end_decimal_count))).intValue();

        long start_time = System.nanoTime();
        List<TSNode> res = new ArrayList<>();
        int firstVal = 0;
        int begin_diff_val = begin_value.intValue() - firstVal;
        int end_diff_val = end_value.intValue() - firstVal;
        IntKeyNode begin_intKeyNode =  bPlusTree.searchKeyNode(bPlusTree.getRoot(bPlusTree), begin_diff_val);
        if (begin_intKeyNode == null)
            return null;
        BPlusDecimalTree begin_PlusDecimalTree = begin_intKeyNode.bPlusDecimalTree;
        KeyNode beginNode = begin_PlusDecimalTree.searchKeyNode(begin_search_value);
//        System.out.println("indidual search" + (System.nanoTime()-start_time)/1000.0);

        IntKeyNode end_intKeyNode =  bPlusTree.searchKeyNode(bPlusTree.getRoot(bPlusTree), end_diff_val);
        if (end_intKeyNode == null)
            return null;
        BPlusDecimalTree end_PlusDecimalTree = end_intKeyNode.bPlusDecimalTree;
        KeyNode endNode = end_PlusDecimalTree.searchKeyNode(end_search_value);

        List<SkiplistNode> results = new ArrayList<>();
        KeyNode temp = beginNode;
        if (beginNode!=null && endNode!=null) {
            while (temp.key != endNode.key)
            {
                if (temp.flagTrueNode != null && temp.flagTrueNode.decimalNodes != null) {
                    for (DecimalNode decimalNode : temp.flagTrueNode.decimalNodes){
                        results.addAll(decimalNode.tsNodeList);
                    }
                }
                if (temp.flagFalseNode != null && temp.flagFalseNode.tsNodeList != null) {
                    results.addAll(temp.flagFalseNode.tsNodeList);
                }
                temp = beginNode.next;
                break;
            }
        }

//        long end_time = System.nanoTime();
//        System.out.println("range search" + (end_time-start_time)/1000.0);

        return results;

    }
    public static void main(String[] args) {

    }
}
