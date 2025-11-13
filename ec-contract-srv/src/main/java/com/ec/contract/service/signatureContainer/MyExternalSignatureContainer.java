package com.ec.contract.service.signatureContainer;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.signatures.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@Slf4j
public class MyExternalSignatureContainer implements IExternalSignatureContainer {

    private PrivateKey pk;
    private Certificate[] chain;



    public MyExternalSignatureContainer(PrivateKey pk, Certificate[] chain) {
        this.pk = pk;
        this.chain = chain;


    }


    public byte[] sign(InputStream is)  {
        try {
            PrivateKeySignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, "SunRsaSign");
            String digestAlgorithm = signature.getHashAlgorithm();
            BouncyCastleDigest digest = new BouncyCastleDigest();

            PdfPKCS7 sgn = new PdfPKCS7(null, chain, digestAlgorithm, null, digest, false);
            byte hash[] = DigestAlgorithms.digest(is, digest.getMessageDigest(digestAlgorithm));
            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, PdfSigner.CryptoStandard.CMS, null, null);
            byte[] extSignature = signature.sign(sh);
            java.security.Signature vrfSignature = java.security.Signature.getInstance("SHA256withRSA");
            X509Certificate certificate = (X509Certificate) chain[0];
            vrfSignature.initVerify(certificate.getPublicKey());
            vrfSignature.update(sh);
            if (vrfSignature.verify(extSignature)) {
                log.info("- Signature cert keystore verified.");
            } else {
                throw new RuntimeException("- Signature cert keystore verification failed.");
            }
            sgn.setExternalDigest(extSignature, null, "RSA");
            byte[] resultSigned = null; // dữ liệu sau khi ghép chữ ký và timestamp

            resultSigned = sgn.getEncodedPKCS7(hash, PdfSigner.CryptoStandard.CMS, null, null, null);

            return resultSigned;
        } catch (Exception e) {
            log.error("lỗi tại hàm ký MyExternalSignatureContainer {}",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void modifySigningDictionary(PdfDictionary signDic) {
    }
}
