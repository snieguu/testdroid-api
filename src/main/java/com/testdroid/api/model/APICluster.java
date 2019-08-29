package com.testdroid.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testdroid.api.APIEntity;
import com.testdroid.api.APIList;
import com.testdroid.api.util.TimeConverter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Łukasz Kajda <lukasz.kajda@bitbar.com>
 */
@XmlRootElement
public class APICluster extends APIEntity {

    @XmlType(namespace = "APICluster", name = "APIClusterState")
    public enum State {
        OFFLINE,
        ONLINE
    }

    private APIList<APIAdminDevice> devices;

    private Boolean enabled = true;

    private String name;

    private State state = State.OFFLINE;

    private Date stateChangeTime = new Date();

    private Date stateTime = new Date();

    private String url;

    private String pluginVersion;

    public APICluster() {
    }

    public APICluster(
            Long id, String name, String url, String pluginVersion, State state, LocalDateTime stateTime,
            LocalDateTime stateChangeTime, Boolean enabled) {
        super(id);
        this.name = name;
        this.url = url;
        this.state = state;
        this.stateTime = TimeConverter.toDate(stateTime);
        this.stateChangeTime = TimeConverter.toDate(stateChangeTime);
        this.enabled = enabled;
        this.pluginVersion = pluginVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getStateTime() {
        return stateTime;
    }

    public void setStateTime(Date stateTime) {
        this.stateTime = stateTime;
    }

    public Date getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(Date stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public APIList<APIAdminDevice> getDevices() {
        return devices;
    }

    public void setDevices(APIList<APIAdminDevice> devices) {
        this.devices = devices;
    }

    @Override
    @JsonIgnore
    protected <T extends APIEntity> void clone(T from) {
        APICluster apiCluster = (APICluster) from;
        cloneBase(from);
        this.devices = apiCluster.devices;
        this.enabled = apiCluster.enabled;
        this.name = apiCluster.name;
        this.state = apiCluster.state;
        this.stateChangeTime = apiCluster.stateChangeTime;
        this.stateTime = apiCluster.stateTime;
        this.url = apiCluster.url;
        this.pluginVersion = apiCluster.pluginVersion;
    }

}
