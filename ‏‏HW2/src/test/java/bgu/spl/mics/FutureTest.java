package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    //private /*static*/ Future<String> obj;

    @Before
    public void setUp() throws Exception {
        //obj = new Future<>();
    }

    @After
    public void tearDown() throws Exception {
        //obj = null;
    }

    @Test
    public void get() {
        /*
        try {
            assertNotNull(obj.get());   //Returns after resolve
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        obj.resolve("test");
             */
    }

    @Test
    public void resolve() {
        /*
        obj.resolve("test1");
        try {
            assertEquals(obj.get(), "test1");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        obj.resolve("test2");
        try {
            assertNotEquals(obj.get(), "test1");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            assertEquals(obj.get(), "test2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         */

    }

    @Test
    public void isDone() {
        /*
        assertFalse(obj.isDone());
        try {
            obj.get();   //in the end of this operation It should be null
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(obj.isDone());


         */
    }

    @Test
    public void testGet() {
        /*
        Thread t = new Thread(()-> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            obj.resolve("done");                         //This thread will resolve this obj after 1 second
        } );
        t.start();
        try {
            assertNull(obj.get(1,TimeUnit.MILLISECONDS));   //testing it after 1 millisecond, shouldn't be resolved
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            t.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            assertEquals("done", obj.get(100,TimeUnit.MILLISECONDS));               //after 2 seconds object should be resolved
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         */
    }
}