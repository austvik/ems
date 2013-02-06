package no.java.ems.domain;

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Address extends ValueObject {

    private final String isoCode;
    private final String zipCode;

    /**
     * @throws IllegalArgumentException if ISO code or ZIP code is {@code null}.
     */
    public Address(final String isoCode, final String zipCode) {
        Validate.notNull(isoCode, "ISO code may not be null.");
        Validate.notNull(zipCode, "ZIP code may not be null.");
        
        this.isoCode = isoCode;
        this.zipCode = zipCode;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getIndexingValue() {
        return isoCode + zipCode;
    }
    
    public static Address valueOf(final String isoCode, final String zipCode) {        
        String iso = isoCode == null || isoCode.length() == 0 ? null : isoCode;
        String zip = zipCode == null || zipCode.length() == 0 ? null : zipCode;
        return new Address(iso, zip);
    }

}
