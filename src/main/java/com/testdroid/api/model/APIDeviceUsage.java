package com.testdroid.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testdroid.api.APIEntity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Adrian Zybala <adrian.zybala@bitbar.com>
 */
@XmlRootElement
public class APIDeviceUsage extends APIEntity {

    private APIDevice device;

    private Long totalDeviceSessions;

    private Long failedDeviceSessions;

    public APIDeviceUsage() {
    }

    public APIDeviceUsage(
            Long id, String displayName, Long softwareVersionId, String releaseVersion, Integer apiLevel,
            Integer creditsPrice, String imagePrefix, Integer imageTop, Integer imageLeft, Integer imageWidth,
            Integer imageHeight, Integer frameExtraWidth, APIDevice.OsType osType, Boolean online, Boolean locked,
            Boolean enabled, Long totalDeviceSessions, Long failedDeviceSessions) {
        super(id);
        this.device = new APIDevice(id, displayName, softwareVersionId, releaseVersion, apiLevel, creditsPrice,
                imagePrefix, imageTop, imageLeft, imageWidth, imageHeight, frameExtraWidth, osType, online, locked,
                enabled, null, null);
        this.totalDeviceSessions = totalDeviceSessions;
        this.failedDeviceSessions = failedDeviceSessions;
    }

    @Override
    @JsonIgnore
    protected <T extends APIEntity> void clone(T from) {
        APIDeviceUsage apiDeviceUsage = (APIDeviceUsage) from;
        cloneBase(from);
        this.device = apiDeviceUsage.device;
        this.totalDeviceSessions = apiDeviceUsage.totalDeviceSessions;
        this.failedDeviceSessions = apiDeviceUsage.failedDeviceSessions;
    }

    public APIDevice getDevice() {
        return device;
    }

    public void setDevice(APIDevice device) {
        this.device = device;
    }

    public Long getTotalDeviceSessions() {
        return totalDeviceSessions;
    }

    public void setTotalDeviceSessions(Long totalDeviceSessions) {
        this.totalDeviceSessions = totalDeviceSessions;
    }

    public Long getFailedDeviceSessions() {
        return failedDeviceSessions;
    }

    public void setFailedDeviceSessions(Long failedDeviceSessions) {
        this.failedDeviceSessions = failedDeviceSessions;
    }
}
