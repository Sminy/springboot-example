package juc;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * 计数器：减法
 *      让一线程阻塞直到另一些线程完成一系列操作才被唤醒。
 */
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "国被灭了！");
                countDownLatch.countDown();
            }, CountryEnum.forEach_countryEnum(i).getRetMessage()).start();
        }

        countDownLatch.await();

        System.out.println(Thread.currentThread().getName() + " 秦国统一中原。");
    }


    public enum CountryEnum {
        ONE(1, "齐"), TWO(2, "楚"), THREE(3, "燕"), FOUR(4, "赵"), FIVE(5, "魏"), SIX(6, "韩");

        private Integer retcode;
        private String retMessage;

        public Integer getRetcode() {
            return retcode;
        }

        public String getRetMessage() {
            return retMessage;
        }

        CountryEnum(Integer retcode, String retMessage) {
            this.retcode = retcode;
            this.retMessage = retMessage;
        }

        public static CountryEnum forEach_countryEnum(int index) {

            CountryEnum[] myArray = CountryEnum.values();

            for (CountryEnum ce : myArray) {
                if (Objects.equals(index, ce.getRetcode())) {
                    return ce;
                }
            }
            return null;
        }

    }
}


