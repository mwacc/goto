package com.pyxis.demo;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kostya on 2/27/15.
 */
public class RunMasterCrawler {

    private BlockingQueue<String> queue = null;

    public RunMasterCrawler() {
        final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        queue = hazelcastInstance.getQueue(Constants.COUNTRIES_QUEUE);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                hazelcastInstance.shutdown();
            }
        } ));

    }

    public static void main(String[] args) {
        new RunMasterCrawler().run(args);
    }

    public void run(String[] args) {
        if( args.length != 1 ) {
            System.err.println("Usage: <path to country file>");
            System.exit(-1);
        }

        // let's put destination counties into queue for processing
        try(BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            for(String line; (line = br.readLine()) != null; ) {
                if( !line.startsWith("#") ) { // ignore comments
                    String[] arr = line.split("\\s", 2);
                    if( arr.length != 2 ) {
                        System.err.println("Correct format for country file is: <country name><TAB><country code> ");
                        continue;
                    }
                    queue.put( arr[0] );
                }
            }
            // line is not visible here.
        } catch (FileNotFoundException e) {
            System.err.println( String.format("File %s not found on filesystem or permissions are incorrect.", args[0]) );
            System.exit(-2);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-3);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-4);
        }

        // now let's wait while all countries would be processed
        while ( queue.size() > 0 ) {
            try {
                Thread.sleep(1 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Master was gracefully switched off");
        System.exit(0);
    }

}
