package com.ec.contract.util;

import com.itextpdf.kernel.font.PdfFont;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class StringUtil {

    public static List<String> combineCharacters(String textFirst, String[] character, Integer length) throws Exception {
        var result = new ArrayList<String>();
        for (String s : character) {
            var endArray = result.size() - 1;
            if (result.isEmpty()) {
                result.add(textFirst + " " + s);
            } else if (Integer.sum(result.get(endArray).length(), s.length() + 1) <= length) {
                result.set(endArray, result.get(endArray) + " " + s);
            } else if (Integer.sum(result.get(endArray).length(), s.length() + 1) > length) {
                result.add(s);
            }
        }
        return result;
    }


    public static String removeSpecialCharToPrompt(String input) {
        String whiteListChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz,.:;0123456789- ";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            for (int j = 0; j < whiteListChars.length(); j++) {
                char allowdChar = whiteListChars.charAt(j);
                if (ch == allowdChar) {
                    sb.append(ch);
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * Tự sinh chuỗi uid contract với độ dài cố định
     *
     * @param length Độ dài cố định của chuỗi
     * @return chuỗi
     */
    public static String generateContractUid(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    public static String generateMessageId(String senderId) {
        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYMM");
        String dateAsString = dateObj.format(formatter);
        return senderId + dateAsString + UUID.randomUUID().toString().toUpperCase(Locale.ROOT).replace("-", "");
    }
    
    public static List<String> getMSTFromCert(String certB64) {
        List<String> result = new ArrayList<>();
    	try {
    		byte encodedCert[] = Base64.getDecoder().decode(certB64);
            ByteArrayInputStream inputStream  =  new ByteArrayInputStream(encodedCert);

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(inputStream);
            
            String dn = cert.getSubjectX500Principal().getName();

            LdapName ldapDN = new LdapName(dn);
            for(Rdn rdn: ldapDN.getRdns()) {
            	if(rdn.getType().equals("UID")) {
                    result.add(rdn.getValue().toString());
            	}
            }
		} catch (Exception e) {
			log.error("Can't get UID from Certificate: ", e);
		} 
    	
    	return result;
    }

    public static String base64ToHex(String base64) {
        String rs = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            final char[] hexArray = "0123456789abcdef".toCharArray();
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0, v; j < bytes.length; j++) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            rs = new String(hexChars);
        }catch (Exception e){
            log.error("Can't convert base64 to hex: ", e);
        }

        return rs;
    }

    public static int generateCode() {
        int code = 0;

        try {
            var random = new Random();
            code = random.nextInt(900000) + 100000; // 6-digit
        } catch (Exception e) {
            log.error("error generate code: {}", e);
        }

        return code;
    }

    public static List<String> autoSplit(String contentStr, float width, PdfFont font, float fontSize) {
        List<String> reList = new ArrayList<String>();
        if (contentStr == null){
            contentStr = "";
        }
        String[] tempSplit = contentStr.split(" ");
        String strTemp = "";
        String strFinal;
        boolean rowFirst = false;
        String reString = "";

        for (int x = 0; x < tempSplit.length; ++x) {
            String content = tempSplit[x];
            if (content.length() > 0) {
                strFinal = strTemp;

                if(strTemp.equals("")) {
                    strTemp += content;
                }else {
                    strTemp += " "+content;
                }

                if (font.getWidth(strTemp, fontSize) > width) {
                    if(reString.equals("")) {
                        reString += strFinal;

                        if(!rowFirst) {
                            reList.add(strFinal);
                            rowFirst = true;
                            reString = "";
                        }
                    }else {
                        reString += "\n"+strFinal;
                    }

                    strTemp = content;
                }
            }
        }

        if(!strTemp.equals("")) {
            if(reString.equals("")) {
                reString += strTemp;
            } else {
                reString += "\n"+strTemp;
            }
        }

        reList.add(reString);

        return reList;
    }

    /**
     * Kiểm tra String là dãy số hay chữ
     * @param strNum chuỗi string
     * @return boolean
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
