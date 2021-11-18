let xml = `
      <bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="sample-diagram" targetNamespace="http://bpmn.io/schema/bpmn" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd">
      <bpmn2:process id="process{str}" name="流程{str}">
        <bpmn2:startEvent id="StartEvent_01ydzqe" name="开始" activiti:initiator="applyUserId"  />
      </bpmn2:process>
      <bpmndi:BPMNDiagram id="BPMNDiagram_1">
        <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="process{str}">
          <bpmndi:BPMNShape id="StartEvent_01ydzqe_di" bpmnElement="StartEvent_01ydzqe">
            <dc:Bounds x="142" y="212" width="36" height="36" />
            <bpmndi:BPMNLabel>
              <dc:Bounds x="149" y="255" width="22" height="14" />
            </bpmndi:BPMNLabel>
          </bpmndi:BPMNShape>
        </bpmndi:BPMNPlane>
      </bpmndi:BPMNDiagram>
    </bpmn2:definitions>`;

export default {
  initTemplate: function (str) {
    return xml.replace(/{str}/g, str)
  }
};
