package com.lordgiacomos.cachedauth.api;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiTestingMain {
    public static void main(String[] args) {
        Authenticator.test();


        /*try {
            String l1_test = Files.readString(Path.of("/home/l_gs/IdeaProjects/OAuth_MCJava/src/main/java/com/lordgiacomos/test.json"));
            MSAResponse parsedAuth = new MSAResponse(l1_test);

            System.out.println(parsedAuth.userCode);
            System.out.println(parsedAuth.deviceCode);
            System.out.println(parsedAuth.verificationUri);
            System.out.println(parsedAuth.expiresInSeconds);
            System.out.println(parsedAuth.interval);
            System.out.println(parsedAuth.message);

        } catch (IOException e) {
            System.out.println("file issues");
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println("uri issues");
            System.out.println(e.getMessage());
        }*/
    }
}