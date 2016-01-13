/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Vibin
 */
public class PostOffice extends Thread {

    private final int customers = 50;
    private final int postalWorkers = 3;
    Semaphore max_customers = new Semaphore(10);
    Semaphore postal_workers = new Semaphore(3);
    Semaphore cust_ready = new Semaphore(0);
    Semaphore[] finished = new Semaphore[50];
    Semaphore scales = new Semaphore(1);
    Semaphore mutex = new Semaphore(1);
    Semaphore[] start = new Semaphore[50];
    Semaphore[] assignWorker = new Semaphore[50];
    Queue<Integer> cus = new LinkedList<>();
    int[] post = new int[customers];

    public void simulate() {
        int i = 0;

        for (i = 0; i < customers; i++) {
            finished[i] = new Semaphore(0);
            start[i] = new Semaphore(0);
            assignWorker[i] = new Semaphore(0);
        }

        Thread[] cust = new Thread[customers];
        Thread[] work = new Thread[postalWorkers];
        System.out.println("Simulating Post Office with 50 customers and 3 postal workers\n");

        for (i = 0; i < customers; i++) {
            int task = (int) (Math.random() * (3)) + 1;
            cust[i] = new Customer(i, task);
            System.out.println("Customer " + i + " created");
            cust[i].start();
        }

        for (i = 0; i < postalWorkers; i++) {
            work[i] = new PostalWorker(i);
            System.out.println("Postal Worker " + i + " created");
            work[i].start();
        }

        for (i = 0; i < customers; i++) {
            try {
                cust[i].join();
                System.out.println("Joined customer " + i);
            } catch (InterruptedException ex) {

            }
        }

        for (i = 0; i < postalWorkers; i++) {
            try {
                work[i].interrupt();
                work[i].join(1);
                System.out.println("Joined Postal Worker " + i);
            } catch (InterruptedException ex) {
                //Logger.getLogger(PostOffice.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.exit(0);
    }

    class Customer extends Thread {

        private int cust_number;
        private int cust_task;

        private Customer(int i, int task) {
            cust_number = i;
            cust_task = task;
        }

        public void run() {
            try {
                max_customers.acquire();
                System.out.println("Customer " + cust_number + " enters post office");
                postal_workers.acquire();

                mutex.acquire();
                cus.add(cust_number);
                cus.add(cust_task);
                cust_ready.release();
                mutex.release();
                
                assignWorker[cust_number].acquire();
                System.out.println("Customer " + cust_number + " asks postal worker " + post[cust_number]+ startTask(cust_task));
                start[cust_number].release();
                finished[cust_number].acquire();                
                System.out.println("Customer " + cust_number + finishTask(cust_task));
                System.out.println("Customer " + cust_number + " leaves post office");
                max_customers.release();
            } catch (InterruptedException ex) {
            }
        }

        private String startTask(int i) {
            switch (i) {
                case 1:
                    return " to buy stamps";
                case 2:
                    return " to mail a letter";
                case 3:
                    return " to mail a package";
                default:
                    return "";
            }
        }

        private String finishTask(int i) {
            switch (i) {
                case 1:
                    return " finished buying stamps";
                case 2:
                    return " finished mailing a letter";
                case 3:
                    return " finished mailing a package";
                default:
                    return "";
            }
        }

    }

    class PostalWorker extends Thread {

        private int work_number;
        private int cust;

        private PostalWorker(int i) {
            work_number = i;
        }

        public void run() {
            int cust_num;
            int cust_task;
            while (true) {
                try {
                    cust_ready.acquire();
                    mutex.acquire();
                    cust_num = cus.poll();
                    cust_task = cus.poll();
                    post[cust_num] = work_number;
                    System.out.println("Postal Worker " + work_number + " serving customer " + cust_num + "");
                    cust = cust_num;
                    assignWorker[cust].release();
                    mutex.release();
                    
                    start[cust].acquire();
                    
                    
                    scaleRes(cust_task);
                    
                    System.out.println("Postal Worker " + work_number + " finished serving customer " + cust);
                    finished[cust].release();
                    postal_workers.release();
                } catch (InterruptedException e) {

                }
            }
        }

        public void scaleRes(int task) throws InterruptedException {
            switch(task){
                case 1:
                    Customer.sleep(1000);
                    break;
                case 2:
                    Customer.sleep(1500);
                    break;
                case 3:
                    scales.acquire();
                    System.out.println("Scales in use by postal worker " + work_number);
                    Customer.sleep(2000);
                    System.out.println("Scales released by postal worker " + work_number);
                    scales.release();
                    break;
                default: break;
            }
        }

    }

    public static void main(String[] args) {
        PostOffice p = new PostOffice();
        p.simulate();
    }
}
