package com.testdroid.api;

import com.testdroid.api.dto.Context;
import com.testdroid.api.filter.BooleanFilterEntry;
import com.testdroid.api.filter.StringFilterEntry;
import com.testdroid.api.model.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.testdroid.api.dto.Operand.EQ;
import static com.testdroid.api.model.APIDevice.OsType.ANDROID;
import static com.testdroid.api.model.APIFileConfig.Action.INSTALL;
import static com.testdroid.api.model.APIFileConfig.Action.RUN_TEST;
import static com.testdroid.api.model.APITestRun.State.WAITING;
import static com.testdroid.cloud.test.categories.TestTags.API_CLIENT;
import static com.testdroid.dao.repository.dto.MappingKey.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Damian Sniezek <damian.sniezek@bitbar.com>
 */
@Tag(API_CLIENT)
public class APIUserAPIClientTest extends APIClientTest{

    @ParameterizedTest
    @ArgumentsSource(APIClientProvider.class)
    public void getDevicesTest(AbstractAPIClient apiKeyClient) throws APIException {
        APIList<APIDevice> allDevices = apiKeyClient.getDevices(new Context<>(APIDevice.class)).getEntity();
        assertThat(allDevices.isEmpty(), is(FALSE));

        APIList<APIDevice> androidDevices = apiKeyClient.getDevices(new Context<>(APIDevice.class)
                .addFilter(new StringFilterEntry(OS_TYPE, EQ, ANDROID.name()))).getEntity();
        assertThat(androidDevices.isEmpty(), is(FALSE));
        assertThat(androidDevices.getData().stream().allMatch(d -> d.getOsType().equals(ANDROID)), is(TRUE));
        assertThat(androidDevices.getTotal(), is(lessThanOrEqualTo(allDevices.getTotal())));

        APIList<APIDevice> samsungDevices = apiKeyClient.getDevices(new Context<>(APIDevice.class)
                .addFilter(new StringFilterEntry(DISPLAY_NAME, EQ, "%Samsung%"))).getEntity();
        assertThat(samsungDevices.getTotal(), is(lessThanOrEqualTo(androidDevices.getTotal())));
    }

    @ParameterizedTest
    @ArgumentsSource(APIClientProvider.class)
    public void getLabelGroups(AbstractAPIClient apiKeyClient) throws APIException {
        APIList<APILabelGroup> labelGroups = apiKeyClient.getLabelGroups(new Context<>(APILabelGroup.class)
                .addFilter(new StringFilterEntry(DISPLAY_NAME, EQ, "Device groups"))).getEntity();
        assertThat(labelGroups.getTotal(), is(1));
        APILabelGroup deviceGroupLabelGroup = labelGroups.getData().stream().findFirst().get();
        assertThat(deviceGroupLabelGroup.getDisplayName(), is("Device groups"));
        APIList<APIDeviceProperty> apiDeviceProperties = deviceGroupLabelGroup.getDevicePropertiesResource(new
                Context<>(APIDeviceProperty.class).setLimit(0)).getEntity();
        assertThat(apiDeviceProperties.getTotal(), is(3));
        assertThat(apiDeviceProperties.getData().stream().map(APIDeviceProperty::getDisplayName)
                .collect(Collectors.toList()
                ), containsInAnyOrder("Android devices", "iOS devices", "Trial devices"));
    }

    @ParameterizedTest
    @ArgumentsSource(APIClientProvider.class)
    public void uploadFileTest(AbstractAPIClient apiKeyClient) throws APIException {
        APIUserFile apiUserFile = apiKeyClient.me()
                .uploadFile(new File(getClass().getResource(APP_PATH).getFile()));
        assertThat(apiUserFile.getName(), is("BitbarSampleApp.apk"));
    }

    @ParameterizedTest
    @ArgumentsSource(APIClientProvider.class)
    public void getAvailableFrameworksTest(AbstractAPIClient apiKeyClient) throws APIException {
        StringFilterEntry osTypeFilter = new StringFilterEntry(OS_TYPE, EQ, ANDROID.name());
        BooleanFilterEntry forProject = new BooleanFilterEntry(FOR_PROJECTS, EQ, TRUE);
        BooleanFilterEntry canRunFromUI = new BooleanFilterEntry(CAN_RUN_FROM_UI, EQ, TRUE);
        Context<APIFramework> context = new Context(APIFramework.class, 0, MAX_VALUE, EMPTY, EMPTY);
        context.addFilter(osTypeFilter);
        context.addFilter(forProject);
        context.addFilter(canRunFromUI);
        APIList<APIFramework> availableFrameworks = apiKeyClient.me().getAvailableFrameworksResource(context)
                .getEntity();
        assertThat(availableFrameworks.getData().stream().allMatch(f -> f.getForProjects() && f.getCanRunFromUI() && f
                .getOsType() == ANDROID), is(TRUE));
    }

    @ParameterizedTest
    @ArgumentsSource(APIClientProvider.class)
    public void startTestRunTest(AbstractAPIClient apiKeyClient) throws APIException {
        APIUser me = apiKeyClient.me();
        APIProject project = me.createProject(APIProject.Type.ANDROID, generateUnique("testProject"));
        APITestRunConfig config = project.getTestRunConfig();
        APIFramework defaultApiFramework = getApiFramework(apiKeyClient, "DEFAULT");
        config.setFrameworkId(defaultApiFramework.getId());
        Long apkFileId = me.uploadFile(new File(APIKeyClient.class.getResource(APP_PATH).getFile())).getId();
        APIFileConfig apkFileConfig = new APIFileConfig(apkFileId, INSTALL);
        Long testFileId = me.uploadFile(new File(APIKeyClient.class.getResource(TEST_PATH).getFile())).getId();
        APIFileConfig testFileConfig = new APIFileConfig(testFileId, RUN_TEST);
        config.setFiles(Arrays.asList(apkFileConfig, testFileConfig));
        me.validateTestRunConfig(config);
        APITestRun apiTestRun = me.startTestRun(config);
        assertThat(apiTestRun.getState(), is(oneOf(RUNNING, WAITING)));
        apiTestRun.abort();
    }

    @ParameterizedTest
    @ArgumentsSource(APIClientProvider.class)
    public void addTagTest(AbstractAPIClient apiKeyClient) throws APIException {
        APIUser me = apiKeyClient.me();
        APIProject project = me.createProject(APIProject.Type.ANDROID, generateUnique("testProject"));
        APITestRunConfig config = project.getTestRunConfig();
        APIFramework defaultApiFramework = getApiFramework(apiKeyClient, "DEFAULT");
        config.setFrameworkId(defaultApiFramework.getId());
        Long apkFileId = me.uploadFile(new File(APIKeyClient.class.getResource(APP_PATH).getFile())).getId();
        APIFileConfig apkFileConfig = new APIFileConfig(apkFileId, INSTALL);
        Long testFileId = me.uploadFile(new File(APIKeyClient.class.getResource(TEST_PATH).getFile())).getId();
        APIFileConfig testFileConfig = new APIFileConfig(testFileId, RUN_TEST);
        config.setFiles(Arrays.asList(apkFileConfig, testFileConfig));
        me.validateTestRunConfig(config);
        APITestRun apiTestRun = me.startTestRun(config);
        assertThat(apiTestRun.getState(), is(oneOf(RUNNING, WAITING)));
        apiTestRun.abort();
        String tag = "aborted";
        APITag apiTag = apiTestRun.addTag(tag);
        apiTestRun.refresh();
        assertThat(apiTestRun.getTagsResource().getTotal(), is(1));
        assertThat(apiTestRun.getTagsResource().getEntity().get(0).getName(), is(tag));
        apiTag.delete();
        apiTestRun.refresh();
        assertThat(apiTestRun.getTagsResource().getTotal(), is(0));
    }

    @ParameterizedTest
    @ArgumentsSource(APIClientProvider.class)
    public void requestScreenshotsZip(AbstractAPIClient apiKeyClient) throws APIException {
        APIUser me = apiKeyClient.me();
        APIProject project = me.createProject(APIProject.Type.ANDROID, generateUnique("testProject"));
        APITestRunConfig config = project.getTestRunConfig();
        APIFramework defaultApiFramework = getApiFramework(apiKeyClient, "DEFAULT");
        config.setFrameworkId(defaultApiFramework.getId());
        Long apkFileId = me.uploadFile(new File(APIKeyClient.class.getResource(APP_PATH).getFile())).getId();
        APIFileConfig apkFileConfig = new APIFileConfig(apkFileId, INSTALL);
        Long testFileId = me.uploadFile(new File(APIKeyClient.class.getResource(TEST_PATH).getFile())).getId();
        APIFileConfig testFileConfig = new APIFileConfig(testFileId, RUN_TEST);
        config.setFiles(Arrays.asList(apkFileConfig, testFileConfig));
        me.validateTestRunConfig(config);
        APITestRun apiTestRun = me.startTestRun(config);
        assertThat(apiTestRun.getState(), is(oneOf(RUNNING, WAITING)));
        apiTestRun.abort();
        apiTestRun.requestScreenshotsZip();
        APIUserFile file = apiTestRun.getScreenshotsZip();
        while (file.getState() != APIUserFile.State.READY) {
            try {
                TimeUnit.SECONDS.sleep(3);
                file.refresh();
            } catch (InterruptedException ignore) {
            }
        }
        file.getFile();
    }

}
