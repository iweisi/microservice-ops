package com.yonyou.cloud.ops.alert.ops.alert.biz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yonyou.cloud.common.beans.PageResultResponse;
import com.yonyou.cloud.common.beans.RestResultResponse;
import com.yonyou.cloud.common.service.BaseService;
import com.yonyou.cloud.common.service.utils.PageQuery;
import com.yonyou.cloud.ops.alert.ops.alert.alarm.AlarmMessageContext;
import com.yonyou.cloud.ops.alert.ops.alert.alarm.mails.EmailMessage;
import com.yonyou.cloud.ops.alert.ops.alert.domain.constants.AlertStatus;
import com.yonyou.cloud.ops.alert.ops.alert.domain.dto.AlertInfoBo;
import com.yonyou.cloud.ops.alert.ops.alert.domain.dto.AlertInfoSearchForm;
import com.yonyou.cloud.ops.alert.ops.alert.domain.dto.GroupUsers;
import com.yonyou.cloud.ops.alert.ops.alert.domain.dto.UserBo;
import com.yonyou.cloud.ops.alert.ops.alert.domain.dto.mail.MessageTemplate;
import com.yonyou.cloud.ops.alert.ops.alert.entity.AlertInfo;
import com.yonyou.cloud.ops.alert.ops.alert.entity.UserGroup;
import com.yonyou.cloud.ops.alert.ops.alert.entity.UserInfo;
import com.yonyou.cloud.ops.alert.ops.alert.feign.IUserService;
import com.yonyou.cloud.ops.alert.ops.alert.mapper.AlertInfoMapper;

/**
 * 
 * @author daniell
 *
 */
@Service
public class AlertInfoBiz extends BaseService<AlertInfoMapper, AlertInfo> {

	@Autowired
	EmailMessage emailMessage;
	@Autowired
	UserInfoBiz userInfoBiz;

	@Autowired
	IUserService iUserService;

	@Autowired
	UserGroupAlertBiz userGroupAlertBiz;

	public PageResultResponse<AlertInfoBo> selectByQuery(AlertInfoSearchForm searchForm) {
		Page<Object> result = PageHelper.startPage(searchForm.getPage(), searchForm.getLimit());
		List<AlertInfoBo> list = mapper.selectAlertBO(searchForm);
		return new PageResultResponse<AlertInfoBo>(result.getTotal(), list);
	}

	public void sendMail() {

		List<AlertInfo> alertbo = mapper.selectAlertAndGroupByStatus(AlertStatus.Trigger.getValue());
		System.out.println("" + alertbo);
		List<MessageTemplate> msgTemp = new ArrayList<MessageTemplate>();
		AlarmMessageContext context = new AlarmMessageContext(emailMessage);
		for (AlertInfo alert : alertbo) {
			alert.getAlertDetail();
			alert.getGroupName();
			MessageTemplate msg = new MessageTemplate();
			msg.setSubject("业务报警邮件");
			msg.setContent(alert.getAlertDetail());

			List<GroupUsers> GroupUserslists = userGroupAlertBiz.getList(alert.getGroupId()).getData();
			Set<String> emailhs = new HashSet();
			for (GroupUsers gus : GroupUserslists) {
				int i = 0;
				for (UserBo user : gus.getMembers()) {
					if (StringUtils.isNotBlank(user.getEmail())) {
						emailhs.add(user.getEmail());
						i++;
					}
				}
				String[] arr = new String[emailhs.size()];
				String[] aa=emailhs.toArray(arr);
				msg.setToAddress(aa);
				if(emailhs.size()!=0) {
					msgTemp.add(msg);
				}
				
			}
			context.AlarmMessage(msgTemp);

			// 更正状态为已通知
			alert.setStatus(AlertStatus.Notice.getValue());
			mapper.updateByPrimaryKey(alert);
		}
	}

}