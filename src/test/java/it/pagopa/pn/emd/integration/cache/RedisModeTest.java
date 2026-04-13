package it.pagopa.pn.emd.integration.cache;

   import org.junit.jupiter.api.Test;

   import static org.junit.jupiter.api.Assertions.assertEquals;
   import static org.junit.jupiter.api.Assertions.assertNotNull;

   class RedisModeTest {

       @Test
       void values() {
           RedisMode[] modes = RedisMode.values();
           assertNotNull(modes);
           assertEquals(2, modes.length);
           assertEquals(RedisMode.SERVERLESS, modes[0]);
           assertEquals(RedisMode.MANAGED, modes[1]);
       }

       @Test
       void valueOf() {
           RedisMode serverlessMode = RedisMode.valueOf("SERVERLESS");
           RedisMode managedMode = RedisMode.valueOf("MANAGED");

           assertEquals(RedisMode.SERVERLESS, serverlessMode);
           assertEquals(RedisMode.MANAGED, managedMode);
       }
   }