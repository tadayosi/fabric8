
package org.fusesource.fabric.apollo.amqp.codec;

import org.fusesource.fabric.apollo.amqp.codec.interfaces.AmqpType;
import org.fusesource.fabric.apollo.amqp.codec.marshaller.TypeRegistry;
import org.fusesource.fabric.apollo.amqp.codec.types.Accepted;
import org.fusesource.fabric.apollo.amqp.codec.types.Header;
import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CodecTest {

    @Test
    public void testTypeRegistry() throws Exception {

        TypeRegistry registry = TypeRegistry.instance();

        for (Byte key : registry.getPrimitiveFormatCodeMap().keySet()) {
            Class clazz = registry.getPrimitiveFormatCodeMap().get(key);
            System.out.printf("0x%x = %s\n", key, clazz.getName());
        }

        for (Long key : registry.getFormatCodeMap().keySet()) {
            Class clazz = registry.getFormatCodeMap().get(key);
            System.out.printf("0x%x = %s\n", key, clazz.getName());
        }

        for (Buffer key : registry.getSymbolicCodeMap().keySet()) {
            Class clazz = registry.getSymbolicCodeMap().get(key);
            System.out.printf("%s = %s\n", key.ascii(), clazz.getName());
        }

        assertTrue(registry.getPrimitiveFormatCodeMap().size() > 0);
        assertTrue(registry.getFormatCodeMap().size() > 0);
        assertTrue(registry.getSymbolicCodeMap().size() > 0);

        AmqpType type = (AmqpType) registry.getSymbolicCodeMap().get(new AsciiBuffer("amqp:header:list")).newInstance();

        System.out.println(String.format("Got type : %s", type.getClass().getName()));

        assertSame("Received type does not match expected type!", type.getClass(), Header.class);
    }


    @Test
    public void testStuff() throws Exception {
        Accepted accepted = new Accepted();
        accepted.setOptions(new HashMap<Buffer, String>());
        accepted.getOptions().put(new Buffer("blah".getBytes()), "blah");

    }

}
