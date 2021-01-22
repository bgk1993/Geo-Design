package com.github.opentech.ipgeo;

public class IpGeo {

  private short country;
  private int city;
  private int postCode;
  private int region;

  private int sicCode;
  private int ispNameCode;
  private short homeBizType;
  private int naicsCode;
  private int cbsaCode;
  private short csaCode;
  private int mdCode;
  private short mcc;
  private short mnc;
  private short connSpeedCode;
  private int orgNameCode;

  public short getCountry() {
    return country;
  }

  public void setCountry(short country) {
    this.country = country;
  }

  public int getCity() {
    return city;
  }

  public void setCity(int city) {
    this.city = city;
  }

  public int getPostCode() {
    return postCode;
  }

  public void setPostCode(int postCode) {
    this.postCode = postCode;
  }

  public int getRegion() {
    return region;
  }

  public void setRegion(int region) {
    this.region = region;
  }

  public int getSicCode() {
    return sicCode;
  }

  public void setSicCode(int sicCode) {
    this.sicCode = sicCode;
  }

  public int getIspNameCode() {
    return ispNameCode;
  }

  public void setIspNameCode(int ispNameCode) {
    this.ispNameCode = ispNameCode;
  }

  public int getNaicsCode() {
    return naicsCode;
  }

  public void setNaicsCode(int naicsCode) {
    this.naicsCode = naicsCode;
  }

  public int getCbsaCode() {
    return cbsaCode;
  }

  public void setCbsaCode(int cbsaCode) {
    this.cbsaCode = cbsaCode;
  }

  public short getCsaCode() {
    return csaCode;
  }

  public void setCsaCode(short csaCode) {
    this.csaCode = csaCode;
  }

  public int getMdCode() {
    return mdCode;
  }

  public void setMdCode(int mdCode) {
    this.mdCode = mdCode;
  }

  public short getMcc() {
    return mcc;
  }

  public void setMcc(short mcc) {
    this.mcc = mcc;
  }

  public short getMnc() {
    return mnc;
  }

  public void setMnc(short mnc) {
    this.mnc = mnc;
  }

  public short getConnSpeedCode() {
    return connSpeedCode;
  }

  public void setConnSpeedCode(short connSpeedCode) {
    this.connSpeedCode = connSpeedCode;
  }

  public int getOrgNameCode() {
    return orgNameCode;
  }

  public void setOrgNameCode(int orgNameCode) {
    this.orgNameCode = orgNameCode;
  }

  public short getHomeBizType() {
    return homeBizType;
  }

  public void setHomeBizType(short homeBizType) {
    this.homeBizType = homeBizType;
  }

  public IpGeo() {

  }

  public IpGeo(Short country, Integer city, Integer postCode, Integer region, Integer sicCode,
      Integer ispNameCode, Short homeBizType, Integer naicsCode, Integer cbsaCode, Short csaCode,
      Integer mdCode, Short mcc, Short mnc, Short connSpeedCode, Integer orgNameCode) {
    super();
    this.country = country;
    this.city = city;
    this.postCode = postCode;
    this.region = region;
    this.sicCode = sicCode;
    this.ispNameCode = ispNameCode;
    this.homeBizType = homeBizType;
    this.naicsCode = naicsCode;
    this.cbsaCode = cbsaCode;
    this.csaCode = csaCode;
    this.mdCode = mdCode;
    this.mcc = mcc;
    this.mnc = mnc;
    this.connSpeedCode = connSpeedCode;
    this.orgNameCode = orgNameCode;
  }

}
