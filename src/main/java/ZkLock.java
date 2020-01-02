import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 基于zookeeper分布式锁
 * @Author newHeart
 * @Create 2020/1/2 20:28
 */
public class ZkLock {
    private ZooKeeper zooKeeper;

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static ZkLock getInstance(){
        return Singleton.getInstance();
    }

    private ZkLock(){
        try {
            zooKeeper = new ZooKeeper("192.168.0.104:2181,192.168.0.112:2181,192.168.0.113:2181",
                    5000, new ZkWatcher());
            System.out.println(zooKeeper.getState());
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("建立连接"+zooKeeper.getState());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void lock(Integer id){
        String path = "/newheart-2020-"+id;
        //创建临时节点，如何创建成功，表示获取到锁,失败，则不断尝试锁
        try {
            zooKeeper.create(path,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("成功获取到锁");
        } catch (Exception e) {
            while (true){
                try {
                    Thread.sleep(300);
                    zooKeeper.create(path,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e1) {
                    continue;
                }
                break;
            }
        }
    }

    public void unlock(Integer id){
        try {
            zooKeeper.delete( "/newheart-2020-"+id,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private class ZkWatcher implements Watcher {
        @Override
        public void process(WatchedEvent watchedEvent) {
            System.out.println("收到监听事件-------->"+watchedEvent);
            if (watchedEvent.getState()==Event.KeeperState.SyncConnected){
                countDownLatch.countDown();
                System.out.println(countDownLatch.getCount());
            }
        }
    }

    /**
     * 单例模式
     */
    private static class Singleton{
        private static ZkLock zkLock;
        static {
            zkLock = new ZkLock();
        }

        private static ZkLock getInstance(){
            return zkLock;
        }
    }
}
