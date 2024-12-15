package com.example.retrievision;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenFetcher {
private  String fcmscope = "https://www.googleapis.com/auth/firebase.messaging";
    public String getAccessToken() throws Exception {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"retrievision\",\n" +
                    "  \"private_key_id\": \"2d8815ad037d52e39a11a618bacad690afa75802\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC53oowaCKwqxTs\\nY99v5PQWUUT3pMQBTZf8cXMphhkwtLClI+kO5J+7KJ/s3K3RoKlHS7FDi717IFgk\\nkgMIJ8SfcGMroZFM/rBEQq2P5jTMX+84/RU1MuZo3QNcD7KzcOOnxYC4Q/3dIBoL\\nMn3dA5R9VVqMuxZI53Zp2AAtyHjSjp7/1TW+gLyzDozwCHmc1+DxYAixJDNppUUY\\nZlYmpZhJ+DFhP3GSOIv0T/Zcw/8qw1NK0kRzA+Spatax8rMKUgeHIpfmzgoYXES+\\nirqjB/hwfmCwgzSxyjepmjb5/svoHdiEfKv7ZD1ijSngmo4Sj7AhUQBa52WaZtM0\\nAX9tIvg1AgMBAAECggEAOs/ox/Jbb61inH3pmF1IwrsKaJWxE3x65/z55Jyy4c96\\nyXY4a2b6B92N5QPa2CbPCWkw+N+BoDEW0tOEtBLpS03jg03HTSaVnbO/eXXtJPIC\\nHvUtvbySXYH5ayrob5D75/09gpC5Bjv/IaXjX4ifdp0IXtlf7seWl10qiVISsQS2\\nXElbLObQmEFez8iSc8v0njD5lg3e8fvIcUpWBnSZs0OnbWww435GsPSW8l6S/WJY\\neHot1C4TJfXYGM/e+/E8DnZQkPk0659Qrn6E58KBddeouB+/r2pQDWhnmt7f2H6Y\\nkfz26krDTWgHI4enK4FmvDPxghfFaFi5A2NQqBuXjQKBgQD8BcqZg4quMnA9Adx8\\na+0+OVEb0U808pUpHaA3gONxlHSAmUV+9uiMrvFT2hOtQzDEtcXIPltBCLBLuR9/\\nOamkdyCxHPg8UOmbRzo4CPQp9KnPatyiNeAr/rTcWIKukf2TNxlTvSfaweDc3Vh7\\n/eIsmB28z+AqJOWer6zGVfymVwKBgQC8zXqsWY1p4T5qL7tZfZ+1S+h19CvK09Cc\\n/0UvC1nIsMgpAkybROUWIZcT5o3V74486/M8VxP5wlHbGIcPeTPWSG6mJWBz6LPg\\nltdjOsSr/+WFzmEI4jr2OkQdXwApPrCMAfpKCwhObdHPkUQiznCGsFaP304AHV8d\\nrVmAAGUGUwKBgD7IGo5fjH6Upg0sl4aPREyJ7sClRbwE6k+Rx8XL39z9hIA8rmlQ\\njOHu+sNOwjQZVt7KAXk+v3DV/0T92vDQpogH/Sbh9pzf8zHxbnljEaAaCOtYzZnD\\nHhMXomp5yiQ+zhKMWUzZBEKkpaWfpllNB7jgNBhDGK6m/mSnkrzd0tCFAoGAAQaM\\nuAr0fbK/iAwiVGYeS/nRlXJyGnP0GiY41fyVzEVJkQ92IxHIvMoJ7A3Aj3zxpNsT\\nQyyl6yFnGG2VrjvWUD+sejKRx49oCrxHBz72eoi1UnjAhi/Uain+3Ln7Yj3474nD\\nNkvBC8wE67+6QwYkDlc+4SExZ0bBkYOzjMJzygkCgYEA1E40/FaaFwdkGqze27uH\\nzFHtSVMKFwxKR9sOEXCe6A1dU9ccUF9ecW57AiFWi0B87Y3oaDDrHARi2pzwD4Sc\\ntau+/w+Us4VIMFwZbNFz1u0BqqpG/r1y9jtq6nv8sGe87TOQqY5J6p/qULKVkuSV\\nL6sGor94TZ+ow9pg6RtBgsY=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-z4ofz@retrievision.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"111975821125611344705\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-z4ofz%40retrievision.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";
            ByteArrayInputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(List.of(fcmscope));
                    googleCredentials.refresh();
            AccessToken accessToken = googleCredentials.getAccessToken();
            if (accessToken != null) {
                return accessToken.getTokenValue();
            } else {
                throw new Exception("Access token could not be fetched.");
            }
        } catch (IOException e) {
            System.err.println("IO Exception while fetching token: " + e.getMessage());
            throw new Exception("Failed to fetch access token", e);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            throw new Exception("Failed to fetch access token", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to fetch access token", e);
        }
    }
    }
