package slib.sglib.io.conf;

import slib.sglib.io.util.GDataValidator;
import slib.sglib.io.util.GFormat;
import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;

/**
 *
 * @author Sébastien Harispe
 */
public class GDataConf extends ParametrableImpl implements CheckableValidity {

    private GFormat format;
    private String loc;

    /**
     *
     * @param format
     */
    public GDataConf(GFormat format) {
        this.format = format;
    }

    /**
     *
     * @param format
     * @param loc
     */
    public GDataConf(GFormat format, String loc) {
        this.format = format;
        this.loc = loc;
    }

    /**
     *
     * @return the format associated to the configuration
     */
    public GFormat getFormat() {
        return format;
    }

    /**
     *
     * @param format the format to consider
     */
    public void setFormat(GFormat format) {
        this.format = format;
    }

    /**
     *
     * @return the location of the file associated to the configuration
     */
    public String getLoc() {
        return loc;
    }

    /**
     *
     * @param loc
     */
    public void setLoc(String loc) {
        this.loc = loc;
    }

    /**
     *
     * @return true if the configuration is valid.
     */
    @Override
    public boolean isValid() {
        return GDataValidator.valid(this);
    }
}
