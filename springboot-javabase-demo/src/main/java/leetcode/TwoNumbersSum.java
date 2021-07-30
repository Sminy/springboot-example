package leetcode;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 题目：两数之和
 * 给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 的那 两个 整数，并返回它们的数组下标。
 * 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素不能使用两遍
 * <p>
 * eg:
 * 输入：nums = [2,7,11,15], target = 9
 * 输出：[0,1]
 * 解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
 */
public class TwoNumbersSum {

    public static void main(String[] args) {
        int[] array = {2, 7, 11, 15};
        int[] result = twoNumbersSum2(array, 13);
        System.out.println(Arrays.toString(result));
    }

    /**
     * 双重循环, 时间复杂度 O2
     *
     * @param array
     * @param target
     * @return
     */
    public static int[] twoNumbersSum1(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] == (target - array[i])) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    /**
     *  哈希（更优解法）
     * @param array
     * @param target
     * @return
     */
    public static int[] twoNumbersSum2(int[] array, int target) {
        HashMap map = new HashMap<>();
        for (int i = 0; i < array.length; i++) {
            int second = target - array[i];
            if (map.containsKey(second)) {
                return new int[]{(int) map.get(second), i};
            }
            map.put(array[i], i);
        }

        return null;
    }
}
