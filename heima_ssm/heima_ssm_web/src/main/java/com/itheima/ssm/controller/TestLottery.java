package com.itheima.ssm.controller;

import java.util.*;
import java.util.stream.Collectors;

public class TestLottery {
    public static void main(String[] args) {
        Random random = new Random();
        /*for (int i=1;i<7;i++){
        System.out.println(random.nextInt(33)+1);
        }
        System.out.println(random.nextInt(16)+1);
        */
        Set<Integer> set = new HashSet<>();
        List<Integer> list = null;
        while (true){
            Integer num = random.nextInt(33)+1;
            set.add(num);
            if(set.size()==6){
                //list = set.stream().map(s -> s.intValue()).collect(Collectors.toList());
                list = set.stream().collect(Collectors.toList());
                Collections.sort(list);
                break;
            }
        }
        System.out.println(set);
        System.out.println(list);
        System.out.println(random.nextInt(16)+1);


        /*List<Integer> list1 = new ArrayList<>();
        for (int i=1;i<34;i++) {
            list1.add(i);
        }
        Collections.shuffle(list1);
        System.out.println(list1);*/

        /*List<Integer> list = new ArrayList<>();
        for (int i=1;i<34;i++) {
            list.add(i);
        }
        System.out.println(list);*/

    }
}
