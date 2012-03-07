Stub HTTP
=========

A stub HTTP server for writing unit and acceptance tests. Use it to:

* Specify a sequence of HTTP interactions and return stub responses
* Specify expected HTTP methods, headers, and entities
* Specify stub responses (including status code, entity, and headers)
* Verifies (by default) that all requests were made (in the correct order)
* Optionally allow requests to happen out of order
* Optionally enable 'nice mock' behaviour (i.e. the test won't fail if a call
is never made)

Getting started
==============

The StubHttp allows you to specify a sequence of calls you expect to be made
(using a similar API to EasyMock) and can be used as a JUnit @Rule, allowing
the Junit framework to handle the setup/teardown for you.

E.g. Consider the scenario where a piece of code makes a HEAD followed by a
GET. The HTTP GET is only completed if a successful status code is returned
from the HEAD request. StubHttp lets you write the following test:

```java
public class MyTest {

    @Rule
    public StubHttp stubHttp = new StubHttp();
    
    @Test
    public void testCodeMakesHEADRequestFollowedByGET() {
        File testZipFileLocation = new File(...);
    
        stubHttp.expect("HEAD", "/foo.zip").andReturn(200);
	stubHttp.expect("GET", "/foo.zip").andReturn(200, );
        stubHttp.replay()
        
        // ...test here...        
    }
    
    @Test
    public void testCodeDoesntDoGETIfHEADRequestIs404() {
        File testZipFileLocation = new File(...);
    
        stubHttp.expect("HEAD", "/foo.zip").andReturn(404);
        stubHttp.replay()
        
        // ...test here...        
    }

}
```