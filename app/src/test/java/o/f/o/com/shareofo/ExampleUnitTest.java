package o.f.o.com.shareofo;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    int pos, limit;

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

        ByteBuffer b = ByteBuffer.allocate(256);
        b.clear();

        b.put("abc".getBytes());

        mark(b);
        dump(b);
        reset(b);
        b.put("def".getBytes());

        mark(b);
        dump(b);
        reset(b);
        b.put("ghi".getBytes());

        mark(b);
        dump(b);
        reset(b);
        b.put("jkl".getBytes());

        dump(b);

    }

    private void mark(ByteBuffer b) {
        limit = b.limit();
        pos = b.position();
    }

    private void reset(ByteBuffer b) {
        b.limit(limit);
        b.position(pos);
    }

    private void dump(ByteBuffer b) {
        b.flip();

        while (b.limit() > b.position()) {
            byte[] d = new byte[3];
            b.get(d);
            System.out.println(new String(d));
        }

        System.out.println("");
    }
}