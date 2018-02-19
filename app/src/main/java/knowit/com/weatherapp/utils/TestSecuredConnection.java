package knowit.com.weatherapp.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * http://stackoverflow.com/questions/7199129/how-to-get-server-certificate-chain-then-verify-its-valid-and-trusted-in-java
 */
public class TestSecuredConnection {

    private boolean valid;
    private Exception exception;
    private CertificateFactory certFactory;
    private InputStream inputStream;

    public TestSecuredConnection() {
        super();
    }

    public TestSecuredConnection(HttpsURLConnection connection) {
        super();
        try {
            testConnection(connection);
        } catch (Exception e) {
            exception = e;
        }
    }

    private void testConnection(HttpsURLConnection connection) throws SSLPeerUnverifiedException, IllegalStateException {
        Certificate[] certificates = connection.getServerCertificates();
        for (Certificate certificate : certificates) {
//            System.out.println("Certificate is: " + certificate);
            if(certificate instanceof X509Certificate) {
                checkValidity((X509Certificate) certificate);
                if (!isValid()) break;
            }
            else {
//                System.out.println("\n\nCertificate is not a X509Certificate instance\n");
//                System.out.println("Certificate is a " + certificate.getType() + " type certificate\n");
//                System.out.println("Certificate is of " + certificate.getClass() + " class\n\n");
                checkValidity(convertCertificate(certificate));
                if (!isValid()) break;
            }
        }
    }

    /**
     * http://stackoverflow.com/questions/11515725/converting-certificate-byte-to-x509certificate-in-java
     * @param certificate the certificate object to convert
     * @return a X509Certificate certificate object
     */
    private X509Certificate convertCertificate(Certificate certificate) {
        try {
            if(certFactory == null) certFactory = CertificateFactory.getInstance("X.509");
            if(inputStream == null) inputStream = new ByteArrayInputStream(certificate.getEncoded());
            return (X509Certificate) certFactory.generateCertificate(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    public Exception getException() {
        return exception;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * This method must remain private to ensure that only this class can determine the validity of the server certificates.
     * @param valid true - valid, false - invalid
     */
    private void setValid(boolean valid) {
        this.valid = valid;
    }

    private void checkValidity(X509Certificate certificate) {
        if (certificate == null) {
            setValid(false);
            return;
        }
        try {
            certificate.checkValidity();
//            System.out.println("Certificate is active for current date");
            setValid(true);
        } catch(CertificateExpiredException e) {
//            System.out.println("Certificate is expired");
            e.printStackTrace();
            setValid(false);
        } catch(Exception e) {
//            System.out.println("Certificate is invalid");
            e.printStackTrace();
            setValid(false);
        }
    }
}