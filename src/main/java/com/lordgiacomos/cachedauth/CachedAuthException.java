package com.lordgiacomos.cachedauth;

public class CachedAuthException extends Exception {
    /*
    very basic custom exception so I'm not just raising generic one --
        I should come back to this & check if there's better stuff later
     */
    public CachedAuthException(String errorMessage) {
        super(errorMessage);
    }

    public CachedAuthException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }



    /*
    realistically, if I keep this class, I might want to subdivide it into a few different variations:
        - internet issue (issues around connection & IO for http requests)
        - parsing issue (when the client can't parse http responses or config)
        ????
        - misc issue

     */
}
