package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.sql.Time;
import java.util.concurrent.*;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;


    @GetMapping("{skuId}.html")
    public String  loadData(@PathVariable("skuId") Long skuId, Model model){
        ItemVo itemVo =  this.itemService.loadData(skuId);
        model.addAttribute("itemVo", itemVo);
        this.itemService.asyncExcute(itemVo);
        return "item";
    }

    public static void main(String[] args) throws IOException {

//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
//        threadPoolExecutor.execute(()->{
//            System.out.println("线程池");
//        });
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("通过CompletableFuture的supplyAsync()方法初始化了一个多线程程序");
//            int i = 1/0;
            return "hello CompletableFuture";
        });
        CompletableFuture<String> future1 = future.thenApplyAsync((t -> {
            System.out.println("=====================thenApplyAsync===================");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("t:" + t);
            return "hello thenApplyAsync";
        }));
        CompletableFuture<Void> future2 = future.thenAcceptAsync(t -> {
            System.out.println("======================thenAcceptAsync=================");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("t: " + t);
        });
        CompletableFuture<Void> future3 = future.thenRunAsync(() -> {
            System.out.println("=======================thenRunAsync===================");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("这是thenRunAsync方法");
        });

        CompletableFuture<Void> future4 = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("这又是一个新的任务");
        });

        CompletableFuture.anyOf(future1,future2,future3,future4).join();


//        .whenCompleteAsync((t,u)->{
//            System.out.println("=======================whenComplete===================");
//            System.out.println("上一个任务的返回结果集：t:" + t);
//            System.out.println("上一个任务的异常信息：u:" + u);
//        }).exceptionally(t->{
//            System.out.println("=======================exceptionally=================");
//            System.out.println("t:" + t);
//            return null;
//        });
//        System.out.println(future.get());
        System.out.println("这是main");
        System.in.read();

    }
}
