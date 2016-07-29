package cn.edu.hfut.dmic.webcollector.util.model;

/**
 * Created by zhoukai1 on 2016/7/15.
 */
public class City {
    private String jianma;
    private String chs;
    private String code;
    private String pinyin;
    private String jianpin;

    private String sequence;

    public City(String jianma, String chs, String code, String pinyin, String jianpin, String sequence) {
        this.jianma = jianma;
        this.chs = chs;
        this.code = code;
        this.pinyin = pinyin;
        this.jianpin = jianpin;
        this.sequence = sequence;
    }

    public City() {

    }

    public String getJianma() {
        return jianma;
    }

    public void setJianma(String jianma) {
        this.jianma = jianma;
    }

    public String getChs() {
        return chs;
    }

    public void setChs(String chs) {
        this.chs = chs;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "City{" +
                "jianma='" + jianma + '\'' +
                ", chs='" + chs + '\'' +
                ", code='" + code + '\'' +
                ", pinyin='" + pinyin + '\'' +
                ", jianpin='" + jianpin + '\'' +
                ", sequence='" + sequence + '\'' +
                '}';
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getJianpin() {
        return jianpin;
    }

    public void setJianpin(String jianpin) {
        this.jianpin = jianpin;
    }
}
