package cn.iocoder.yudao.module.bpm.service.task.cc.dto;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ReadOnlyDelegateExecution;
import org.flowable.variable.api.persistence.entity.VariableInstance;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 仅为了传输processInstanceId
 */
public class BpmDelegateExecutionDTO implements DelegateExecution {

    public BpmDelegateExecutionDTO(String getProcessInstanceId) {
        this.getProcessInstanceId = getProcessInstanceId;
    }

    private final String getProcessInstanceId;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getProcessInstanceId() {
        return null;
    }

    @Override
    public String getRootProcessInstanceId() {
        return null;
    }

    @Override
    public String getEventName() {
        return null;
    }

    @Override
    public void setEventName(String eventName) {

    }

    @Override
    public String getProcessInstanceBusinessKey() {
        return null;
    }

    @Override
    public String getProcessInstanceBusinessStatus() {
        return null;
    }

    @Override
    public String getProcessDefinitionId() {
        return null;
    }

    @Override
    public String getPropagatedStageInstanceId() {
        return null;
    }

    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public String getSuperExecutionId() {
        return null;
    }

    @Override
    public String getCurrentActivityId() {
        return null;
    }

    @Override
    public String getTenantId() {
        return null;
    }

    @Override
    public FlowElement getCurrentFlowElement() {
        return null;
    }

    @Override
    public void setCurrentFlowElement(FlowElement flowElement) {

    }

    @Override
    public FlowableListener getCurrentFlowableListener() {
        return null;
    }

    @Override
    public void setCurrentFlowableListener(FlowableListener currentListener) {

    }

    @Override
    public ReadOnlyDelegateExecution snapshotReadOnly() {
        return null;
    }

    @Override
    public DelegateExecution getParent() {
        return null;
    }

    @Override
    public List<? extends DelegateExecution> getExecutions() {
        return null;
    }

    @Override
    public void setActive(boolean isActive) {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isEnded() {
        return false;
    }

    @Override
    public void setConcurrent(boolean isConcurrent) {

    }

    @Override
    public boolean isConcurrent() {
        return false;
    }

    @Override
    public boolean isProcessInstanceType() {
        return false;
    }

    @Override
    public void inactivate() {

    }

    @Override
    public boolean isScope() {
        return false;
    }

    @Override
    public void setScope(boolean isScope) {

    }

    @Override
    public boolean isMultiInstanceRoot() {
        return false;
    }

    @Override
    public void setMultiInstanceRoot(boolean isMultiInstanceRoot) {

    }

    @Override
    public Map<String, Object> getVariables() {
        return null;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances() {
        return null;
    }

    @Override
    public Map<String, Object> getVariables(Collection<String> collection) {
        return null;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances(Collection<String> collection) {
        return null;
    }

    @Override
    public Map<String, Object> getVariables(Collection<String> collection, boolean b) {
        return null;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstances(Collection<String> collection, boolean b) {
        return null;
    }

    @Override
    public Map<String, Object> getVariablesLocal() {
        return null;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal() {
        return null;
    }

    @Override
    public Map<String, Object> getVariablesLocal(Collection<String> collection) {
        return null;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> collection) {
        return null;
    }

    @Override
    public Map<String, Object> getVariablesLocal(Collection<String> collection, boolean b) {
        return null;
    }

    @Override
    public Map<String, VariableInstance> getVariableInstancesLocal(Collection<String> collection, boolean b) {
        return null;
    }

    @Override
    public Object getVariable(String s) {
        return null;
    }

    @Override
    public VariableInstance getVariableInstance(String s) {
        return null;
    }

    @Override
    public Object getVariable(String s, boolean b) {
        return null;
    }

    @Override
    public VariableInstance getVariableInstance(String s, boolean b) {
        return null;
    }

    @Override
    public Object getVariableLocal(String s) {
        return null;
    }

    @Override
    public VariableInstance getVariableInstanceLocal(String s) {
        return null;
    }

    @Override
    public Object getVariableLocal(String s, boolean b) {
        return null;
    }

    @Override
    public VariableInstance getVariableInstanceLocal(String s, boolean b) {
        return null;
    }

    @Override
    public <T> T getVariable(String s, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> T getVariableLocal(String s, Class<T> aClass) {
        return null;
    }

    @Override
    public Set<String> getVariableNames() {
        return null;
    }

    @Override
    public Set<String> getVariableNamesLocal() {
        return null;
    }

    @Override
    public void setVariable(String s, Object o) {

    }

    @Override
    public void setVariable(String s, Object o, boolean b) {

    }

    @Override
    public Object setVariableLocal(String s, Object o) {
        return null;
    }

    @Override
    public Object setVariableLocal(String s, Object o, boolean b) {
        return null;
    }

    @Override
    public void setVariables(Map<String, ?> map) {

    }

    @Override
    public void setVariablesLocal(Map<String, ?> map) {

    }

    @Override
    public boolean hasVariables() {
        return false;
    }

    @Override
    public boolean hasVariablesLocal() {
        return false;
    }

    @Override
    public boolean hasVariable(String s) {
        return false;
    }

    @Override
    public boolean hasVariableLocal(String s) {
        return false;
    }

    @Override
    public void removeVariable(String s) {

    }

    @Override
    public void removeVariableLocal(String s) {

    }

    @Override
    public void removeVariables(Collection<String> collection) {

    }

    @Override
    public void removeVariablesLocal(Collection<String> collection) {

    }

    @Override
    public void removeVariables() {

    }

    @Override
    public void removeVariablesLocal() {

    }

    @Override
    public void setTransientVariable(String s, Object o) {

    }

    @Override
    public void setTransientVariableLocal(String s, Object o) {

    }

    @Override
    public void setTransientVariables(Map<String, Object> map) {

    }

    @Override
    public Object getTransientVariable(String s) {
        return null;
    }

    @Override
    public Map<String, Object> getTransientVariables() {
        return null;
    }

    @Override
    public void setTransientVariablesLocal(Map<String, Object> map) {

    }

    @Override
    public Object getTransientVariableLocal(String s) {
        return null;
    }

    @Override
    public Map<String, Object> getTransientVariablesLocal() {
        return null;
    }

    @Override
    public void removeTransientVariableLocal(String s) {

    }

    @Override
    public void removeTransientVariable(String s) {

    }

    @Override
    public void removeTransientVariables() {

    }

    @Override
    public void removeTransientVariablesLocal() {

    }
}
