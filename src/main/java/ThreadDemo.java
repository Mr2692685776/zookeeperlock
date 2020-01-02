/**
 * @Author newHeart
 * @Create 2020/1/2 21:07
 */
public class ThreadDemo {

    private static int count = 0;

    private static ZkLock zkLock = ZkLock.getInstance();

    private static void increate(){
        zkLock.lock(1);
        count++;
        System.out.println(count);
        zkLock.unlock(1);
    }

    public static void main(String[] args) {
        for (int i = 0; i <10 ; i++) {
            new Thread(()->{
                for (int j = 0; j < 50; j++) {
                    increate();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
