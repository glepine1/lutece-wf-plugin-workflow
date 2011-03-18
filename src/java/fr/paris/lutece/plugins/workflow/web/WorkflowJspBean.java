/*
 * Copyright (c) 2002-2009, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflow.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.workflow.business.ActionFilter;
import fr.paris.lutece.plugins.workflow.business.ActionHome;
import fr.paris.lutece.plugins.workflow.business.IconHome;
import fr.paris.lutece.plugins.workflow.business.StateFilter;
import fr.paris.lutece.plugins.workflow.business.StateHome;
import fr.paris.lutece.plugins.workflow.business.TaskRemovalListenerService;
import fr.paris.lutece.plugins.workflow.business.WorkflowFilter;
import fr.paris.lutece.plugins.workflow.business.WorkflowHome;
import fr.paris.lutece.plugins.workflow.business.task.ITask;
import fr.paris.lutece.plugins.workflow.business.task.ITaskType;
import fr.paris.lutece.plugins.workflow.business.task.TaskHome;
import fr.paris.lutece.plugins.workflow.service.ActionResourceIdService;
import fr.paris.lutece.plugins.workflow.service.WorkflowService;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.workflow.Action;
import fr.paris.lutece.portal.business.workflow.Icon;
import fr.paris.lutece.portal.business.workflow.State;
import fr.paris.lutece.portal.business.workflow.Workflow;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workflow.WorkflowRemovalListenerService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;


/**
 * class ManageDirectoryJspBean
 */
public class WorkflowJspBean extends PluginAdminPageJspBean
{
    public static final String RIGHT_MANAGE_DIRECTORY = "WORKFLOW_MANAGEMENT";

    // jsp
    private static final String JSP_MODIFY_WORKFLOW = "jsp/admin/plugins/workflow/ModifyWorkflow.jsp";
    private static final String JSP_MODIFY_TASK = "jsp/admin/plugins/workflow/ModifyTask.jsp";
    private static final String JSP_MODIFY_ACTION = "jsp/admin/plugins/workflow/ModifyAction.jsp";
    private static final String JSP_DO_REMOVE_WORKFLOW = "jsp/admin/plugins/workflow/DoRemoveWorkflow.jsp";
    private static final String JSP_DO_REMOVE_STATE = "jsp/admin/plugins/workflow/DoRemoveState.jsp";
    private static final String JSP_DO_REMOVE_ACTION = "jsp/admin/plugins/workflow/DoRemoveAction.jsp";
    private static final String JSP_DO_REMOVE_TASK = "jsp/admin/plugins/workflow/DoRemoveTask.jsp";
    private static final String JSP_MANAGE_WORKFLOW = "jsp/admin/plugins/workflow/ManageWorkflow.jsp";

    // templates
    private static final String TEMPLATE_MANAGE_WORKFLOW = "admin/plugins/workflow/manage_workflow.html";
    private static final String TEMPLATE_CREATE_WORKFLOW = "admin/plugins/workflow/create_workflow.html";
    private static final String TEMPLATE_MODIFY_WORKFLOW = "admin/plugins/workflow/modify_workflow.html";
    private static final String TEMPLATE_CREATE_STATE = "admin/plugins/workflow/create_state.html";
    private static final String TEMPLATE_MODIFY_STATE = "admin/plugins/workflow/modify_state.html";
    private static final String TEMPLATE_CREATE_ACTION = "admin/plugins/workflow/create_action.html";
    private static final String TEMPLATE_MODIFY_ACTION = "admin/plugins/workflow/modify_action.html";
    private static final String TEMPLATE_MODIFY_TASK = "admin/plugins/workflow/modify_task.html";
    private static final String TEMPLATE_MANAGE_ADVANCED_PARAMETERS = "admin/plugins/workflow/manage_advanced_parameters.html";

    // parameters
    private static final String PARAMETER_IS_ENABLED = "is_enabled";
    private static final String PARAMETER_IS_INITIAL_STATE = "is_initial_state";
    private static final String PARAMETER_IS_REQUIRED_WORKGROUP_ASSIGNED = "is_required_workgroup_assigned";
    private static final String PARAMETER_WORKGROUP = "workgroup";
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    private static final String PARAMETER_NAME = "name";
    private static final String PARAMETER_DESCRIPTION = "description";
    private static final String PARAMETER_CANCEL = "cancel";
    private static final String PARAMETER_ID_WORKFLOW = "id_workflow";
    private static final String PARAMETER_ID_ACTION = "id_action";
    private static final String PARAMETER_ID_STATE = "id_state";
    private static final String PARAMETER_ID_TASK = "id_task";
    private static final String PARAMETER_ID_ICON = "id_icon";
    private static final String PARAMETER_ID_AUTOMATIC = "automatic";
    private static final String PARAMETER_ID_STATE_BEFORE = "id_state_before";
    private static final String PARAMETER_ID_STATE_AFTER = "id_state_after";
    private static final String PARAMETER_APPLY = "apply";
    private static final String PARAMETER_PAGE_INDEX_STATE = "page_index_state";
    private static final String PARAMETER_PAGE_INDEX_ACTION = "page_index_action";
    private static final String PARAMETER_ITEMS_PER_PAGE_STATE = "items_per_page_state";
    private static final String PARAMETER_ITEMS_PER_PAGE_ACTION = "items_per_page_action";
    private static final String PARAMETER_TASK_TYPE_KEY = "task_type_key";

    // properties
    private static final String PROPERTY_MANAGE_WORKFLOW_PAGE_TITLE = "workflow.manage_workflow.page_title";
    private static final String PROPERTY_CREATE_WORKFLOW_PAGE_TITLE = "workflow.create_workflow.page_title";
    private static final String PROPERTY_MODIFY_WORKFLOW_PAGE_TITLE = "workflow.modify_workflow.page_title";
    private static final String PROPERTY_CREATE_STATE_PAGE_TITLE = "workflow.create_state.page_title";
    private static final String PROPERTY_MODIFY_STATE_PAGE_TITLE = "workflow.modify_state.page_title";
    private static final String PROPERTY_CREATE_ACTION_PAGE_TITLE = "workflow.create_action.page_title";
    private static final String PROPERTY_MODIFY_ACTION_PAGE_TITLE = "workflow.modify_action.page_title";
    private static final String PROPERTY_MODIFY_TASK_PAGE_TITLE = "workflow.modify_task.page_title";
    private static final String PROPERTY_ITEM_PER_PAGE = "workflow.itemsPerPage";
    private static final String PROPERTY_ALL = "workflow.manage_workflow.select.all";
    private static final String PROPERTY_YES = "workflow.manage_workflow.select.yes";
    private static final String PROPERTY_NO = "workflow.manage_workflow.select.no";
    private static final String FIELD_WORKFLOW_NAME = "workflow.create_workflow.label_name";
    private static final String FIELD_ACTION_NAME = "workflow.create_action.label_name";
    private static final String FIELD_STATE_NAME = "workflow.create_state.label_name";
    private static final String FIELD_WORKFLOW_DESCRIPTION = "workflow.create_workflow.label_description";
    private static final String FIELD_ACTION_DESCRIPTION = "workflow.create_action.label_description";
    private static final String FIELD_STATE_DESCRIPTION = "workflow.create_state.label_description";
    private static final String FIELD_STATE_BEFORE = "workflow.create_action.label_state_before";
    private static final String FIELD_STATE_AFTER = "workflow.create_action.label_state_after";
    private static final String FIELD_ICON = "workflow.create_action.label_icon";

    // mark
    private static final String MARK_PLUGIN = "plugin";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_PAGINATOR_ACTION = "paginator_action";
    private static final String MARK_PAGINATOR_STATE = "paginator_state";
    private static final String MARK_USER_WORKGROUP_REF_LIST = "user_workgroup_list";
    private static final String MARK_USER_WORKGROUP_SELECTED = "user_workgroup_selected";
    private static final String MARK_ACTIVE_REF_LIST = "active_list";
    private static final String MARK_ACTIVE_SELECTED = "active_selected";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_NB_ITEMS_PER_PAGE_ACTION = "nb_items_per_page_action";
    private static final String MARK_NB_ITEMS_PER_PAGE_STATE = "nb_items_per_page_state";
    private static final String MARK_WORKFLOW_LIST = "workflow_list";
    private static final String MARK_STATE_LIST = "state_list";
    private static final String MARK_ACTION_LIST = "action_list";
    private static final String MARK_ICON_LIST = "icon_list";
    private static final String MARK_TASK_TYPE_LIST = "task_type_list";
    private static final String MARK_TASK_LIST = "task_list";
    private static final String MARK_TASK_CONFIG = "task_config";
    private static final String MARK_TASK = "task";
    private static final String MARK_LOCALE = "locale";
    private static final String MARK_WORKFLOW = "workflow";
    private static final String MARK_STATE = "state";
    private static final String MARK_NUMBER_STATE = "number_state";
    private static final String MARK_NUMBER_ACTION = "number_action";
    private static final String MARK_ACTION = "action";
    private static final String MARK_INITIAL_STATE = "initial_state";
    private static final String MARK_PERMISSION_MANAGE_ADVANCED_PARAMETERS = "permission_manage_advanced_parameters";
    private static final String MARK_DEFAULT_VALUE_WORKGROUP_KEY = "workgroup_key_default_value";
    
    // MESSAGES
    private static final String MESSAGE_MANDATORY_FIELD = "workflow.message.mandatory.field";
    private static final String MESSAGE_ERROR_AUTOMATIC_FIELD = "workflow.message.error.automatic.field";
    private static final String MESSAGE_CONFIRM_REMOVE_WORKFLOW = "workflow.message.confirm_remove_workflow";
    private static final String MESSAGE_CONFIRM_REMOVE_STATE = "workflow.message.confirm_remove_state";
    private static final String MESSAGE_CONFIRM_REMOVE_ACTION = "workflow.message.confirm_remove_action";
    private static final String MESSAGE_CONFIRM_REMOVE_TASK = "workflow.message.confirm_remove_task";
    private static final String MESSAGE_INITIAL_STATE_ALREADY_EXIST = "workflow.message.initial_state_already_exist";
    private static final String MESSAGE_CAN_NOT_REMOVE_STATE_ACTIONS_ARE_ASSOCIATE = "workflow.message.can_not_remove_state_actions_are_associate";
    private static final String MESSAGE_CAN_NOT_REMOVE_WORKFLOW = "workflow.message.can_not_remove_workflow";
    private static final String MESSAGE_CAN_NOT_REMOVE_TASK = "workflow.message.can_not_remove_task";
    private static final String MESSAGE_CAN_NOT_DISABLE_WORKFLOW = "workflow.message.can_not_disable_workflow";
    private static final String MESSAGE_TASK_IS_NOT_AUTOMATIC = "workflow.message.task_not_automatic";

    // session fields
    private int _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_ITEM_PER_PAGE, 50 );
    private String _strCurrentPageIndexWorkflow;
    private int _nItemsPerPageWorkflow;
    private String _strCurrentPageIndexState;
    private int _nItemsPerPageState;
    private String _strCurrentPageIndexAction;
    private int _nItemsPerPageAction;
    private int _nIsEnabled = -1;
    private String _strWorkGroup = AdminWorkgroupService.ALL_GROUPS;

    /*-------------------------------MANAGEMENT  WORKFLOW-----------------------------*/

    /**
     * Return management page of plugin workflow
     *
     * @param request
     *            The Http request
     * @return Html management page of plugin workflow
     */
    public String getManageWorkflow( HttpServletRequest request )
    {
        setPageTitleProperty( WorkflowUtils.EMPTY_STRING );

        String strWorkGroup = request.getParameter( PARAMETER_WORKGROUP );
        String strIsEnabled = request.getParameter( PARAMETER_IS_ENABLED );
        _strCurrentPageIndexWorkflow = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX,
                _strCurrentPageIndexWorkflow );
        _nItemsPerPageWorkflow = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE,
                _nItemsPerPageWorkflow, _nDefaultItemsPerPage );

        if ( ( strIsEnabled != null ) && !strIsEnabled.equals( WorkflowUtils.EMPTY_STRING ) )
        {
            _nIsEnabled = WorkflowUtils.convertStringToInt( strIsEnabled );
        }

        if ( ( strWorkGroup != null ) && !strWorkGroup.equals( WorkflowUtils.EMPTY_STRING ) )
        {
            _strWorkGroup = strWorkGroup;
        }

        // build Filter
        WorkflowFilter filter = new WorkflowFilter(  );
        filter.setIsEnabled( _nIsEnabled );
        filter.setWorkGroup( _strWorkGroup );

        List<Workflow> listWorkflow = WorkflowHome.getListWorkflowsByFilter( filter, getPlugin(  ) );
        listWorkflow = ( List<Workflow> ) AdminWorkgroupService.getAuthorizedCollection( listWorkflow, getUser(  ) );

        LocalizedPaginator paginator = new LocalizedPaginator( listWorkflow, _nItemsPerPageWorkflow, getJspManageWorkflow( request ),
                PARAMETER_PAGE_INDEX, _strCurrentPageIndexWorkflow, getLocale(  ) );

        boolean bPermissionAdvancedParameter = RBACService.isAuthorized( Action.RESOURCE_TYPE,
                RBAC.WILDCARD_RESOURCES_ID, ActionResourceIdService.PERMISSION_MANAGE_ADVANCED_PARAMETERS,
                getUser(  ) );
        
        Map<String, Object> model = new HashMap<String, Object>(  );

        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, WorkflowUtils.EMPTY_STRING + _nItemsPerPageWorkflow );
        model.put( MARK_USER_WORKGROUP_REF_LIST, AdminWorkgroupService.getUserWorkgroups( getUser(  ), getLocale(  ) ) );
        model.put( MARK_USER_WORKGROUP_SELECTED, _strWorkGroup );
        model.put( MARK_ACTIVE_REF_LIST, getRefListActive( getLocale(  ) ) );
        model.put( MARK_ACTIVE_SELECTED, _nIsEnabled );
        model.put( MARK_WORKFLOW_LIST, paginator.getPageItems(  ) );
        model.put( MARK_PERMISSION_MANAGE_ADVANCED_PARAMETERS, bPermissionAdvancedParameter );

        setPageTitleProperty( PROPERTY_MANAGE_WORKFLOW_PAGE_TITLE );

        HtmlTemplate templateList = AppTemplateService.getTemplate( TEMPLATE_MANAGE_WORKFLOW, getLocale(  ), model );

        return getAdminPage( templateList.getHtml(  ) );
    }

    /**
     * Gets the workflow creation page
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The workflow creation page
     */
    public String getCreateWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        AdminUser adminUser = getUser(  );
        Locale locale = getLocale(  );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_USER_WORKGROUP_REF_LIST, AdminWorkgroupService.getUserWorkgroups( adminUser, locale ) );
        model.put( MARK_DEFAULT_VALUE_WORKGROUP_KEY, AdminWorkgroupService.ALL_GROUPS );
        setPageTitleProperty( PROPERTY_CREATE_WORKFLOW_PAGE_TITLE );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_WORKFLOW, locale, model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Perform the workflow creation
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doCreateWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        if ( ( request.getParameter( PARAMETER_CANCEL ) == null ) )
        {
            Plugin plugin = getPlugin(  );
            Workflow workflow = new Workflow(  );
            String strError = getWorkflowData( request, workflow, getLocale(  ) );

            if ( strError != null )
            {
                return strError;
            }

            workflow.setCreationDate( WorkflowUtils.getCurrentTimestamp(  ) );
            WorkflowHome.create( workflow, plugin );
        }

        return getJspManageWorkflow( request );
    }

    /**
     * Gets the workflow creation page
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The workflow creation page
     */
    public String getModifyWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        AdminUser adminUser = getUser(  );
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );
        Workflow workflow = null;

        if ( nIdWorkflow != WorkflowUtils.CONSTANT_ID_NULL )
        {
            workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );
        }

        if ( ( workflow == null ) )
        {
            throw new AccessDeniedException(  );
        }

        StateFilter stateFilter = new StateFilter(  );
        stateFilter.setIdWorkflow( nIdWorkflow );

        List<State> listState = StateHome.getListStateByFilter( stateFilter, getPlugin(  ) );

        _strCurrentPageIndexState = Paginator.getPageIndex( request, PARAMETER_PAGE_INDEX_STATE,
                _strCurrentPageIndexState );
        _nItemsPerPageState = Paginator.getItemsPerPage( request, PARAMETER_ITEMS_PER_PAGE_STATE, _nItemsPerPageState,
                _nDefaultItemsPerPage );

        LocalizedPaginator paginatorState = new LocalizedPaginator( listState, _nItemsPerPageState,
                getJspModifyWorkflow( request, nIdWorkflow ), PARAMETER_PAGE_INDEX_STATE, _strCurrentPageIndexState, PARAMETER_ITEMS_PER_PAGE_STATE, 
                getLocale(  ) );

        ActionFilter actionFilter = new ActionFilter(  );
        actionFilter.setIdWorkflow( nIdWorkflow );

        List<Action> listAction = ActionHome.getListActionByFilter( actionFilter, getPlugin(  ) );

        for ( Action action : listAction )
        {
            action.setStateBefore( StateHome.findByPrimaryKey( action.getStateBefore(  ).getId(  ), getPlugin(  ) ) );
            action.setStateAfter( StateHome.findByPrimaryKey( action.getStateAfter(  ).getId(  ), getPlugin(  ) ) );
        }

        _strCurrentPageIndexAction = Paginator.getPageIndex( request, PARAMETER_PAGE_INDEX_ACTION,
                _strCurrentPageIndexAction );
        _nItemsPerPageAction = Paginator.getItemsPerPage( request, PARAMETER_ITEMS_PER_PAGE_ACTION,
                _nItemsPerPageAction, _nDefaultItemsPerPage );

        LocalizedPaginator paginatorAction = new LocalizedPaginator( listAction, _nItemsPerPageAction,
                getJspModifyWorkflow( request, nIdWorkflow ), PARAMETER_PAGE_INDEX_ACTION, _strCurrentPageIndexAction, PARAMETER_ITEMS_PER_PAGE_ACTION, 
                getLocale(  ) );

        workflow.setAllActions( paginatorAction.getPageItems(  ) );
        workflow.setAllStates( paginatorState.getPageItems(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_USER_WORKGROUP_REF_LIST, AdminWorkgroupService.getUserWorkgroups( adminUser, getLocale(  ) ) );
        model.put( MARK_WORKFLOW, workflow );
        model.put( MARK_PAGINATOR_STATE, paginatorState );
        model.put( MARK_PAGINATOR_ACTION, paginatorAction );
        model.put( MARK_STATE_LIST, paginatorState.getPageItems(  ) );
        model.put( MARK_ACTION_LIST, paginatorAction.getPageItems(  ) );
        model.put( MARK_NUMBER_STATE, listState.size(  ) );
        model.put( MARK_NUMBER_ACTION, listAction.size(  ) );
        model.put( MARK_NB_ITEMS_PER_PAGE_STATE, WorkflowUtils.EMPTY_STRING + _nItemsPerPageState );
        model.put( MARK_NB_ITEMS_PER_PAGE_ACTION, WorkflowUtils.EMPTY_STRING + _nItemsPerPageAction );

        setPageTitleProperty( PROPERTY_MODIFY_WORKFLOW_PAGE_TITLE );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_WORKFLOW, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Perform the workflow modification
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doModifyWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        if ( request.getParameter( PARAMETER_CANCEL ) == null )
        {
            String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
            int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );
            Workflow workflow = null;

            if ( nIdWorkflow != WorkflowUtils.CONSTANT_ID_NULL )
            {
                workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );

                if ( ( workflow == null ) )
                {
                    throw new AccessDeniedException(  );
                }

                String strError = getWorkflowData( request, workflow, getLocale(  ) );

                if ( strError != null )
                {
                    return strError;
                }

                WorkflowHome.update( workflow, getPlugin(  ) );

                if ( request.getParameter( PARAMETER_APPLY ) != null )
                {
                    return getJspModifyWorkflow( request, nIdWorkflow );
                }
            }
        }

        return getJspManageWorkflow( request );
    }

    /**
     * Gets the confirmation page of remove all Directory Record
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return the confirmation page of delete all Directory Record
     */
    public String getConfirmRemoveWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );

        UrlItem url = new UrlItem( JSP_DO_REMOVE_WORKFLOW );
        url.addParameter( PARAMETER_ID_WORKFLOW, strIdWorkflow );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_WORKFLOW, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Remove all workflow record of the workflow
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doRemoveWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );

        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );

        ArrayList<String> listErrors = new ArrayList<String>(  );

        if ( !WorkflowRemovalListenerService.getService(  ).checkForRemoval( strIdWorkflow, listErrors, getLocale(  ) ) )
        {
            String strCause = AdminMessageService.getFormattedList( listErrors, getLocale(  ) );
            Object[] args = { strCause };

            return AdminMessageService.getMessageUrl( request, MESSAGE_CAN_NOT_REMOVE_WORKFLOW, args,
                AdminMessage.TYPE_STOP );
        }

        WorkflowHome.remove( nIdWorkflow, getPlugin(  ) );

        return getJspManageWorkflow( request );
    }

    /**
     * Remove all workflow record of the workflow
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doEnableWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );
        Workflow workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );

        if ( ( workflow == null ) )
        {
            throw new AccessDeniedException(  );
        }

        workflow.setEnabled( true );
        WorkflowHome.update( workflow, getPlugin(  ) );

        return getJspManageWorkflow( request );
    }

    /**
     * Remove all workflow record of the workflow
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doDisableWorkflow( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );
        Workflow workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );
        ArrayList<String> listErrors = new ArrayList<String>(  );

        if ( ( workflow == null ) )
        {
            throw new AccessDeniedException(  );
        }

        if ( !WorkflowRemovalListenerService.getService(  ).checkForRemoval( strIdWorkflow, listErrors, getLocale(  ) ) )
        {
            String strCause = AdminMessageService.getFormattedList( listErrors, getLocale(  ) );
            Object[] args = { strCause };

            return AdminMessageService.getMessageUrl( request, MESSAGE_CAN_NOT_DISABLE_WORKFLOW, args,
                AdminMessage.TYPE_STOP );
        }

        workflow.setEnabled( false );
        WorkflowHome.update( workflow, getPlugin(  ) );

        return getJspManageWorkflow( request );
    }

    /**
     * set the data of the workflow in the workflow object
     * @param request  The HTTP request
     * @param workflow the workflow object
     * @param locale the locale
     * @return null if no error appear
     */
    private String getWorkflowData( HttpServletRequest request, Workflow workflow, Locale locale )
    {
        String strName = request.getParameter( PARAMETER_NAME );
        String strDescription = request.getParameter( PARAMETER_DESCRIPTION );

        String strWorkgroup = request.getParameter( PARAMETER_WORKGROUP );
        String strFieldError = WorkflowUtils.EMPTY_STRING;

        if ( ( strName == null ) || strName.trim(  ).equals( WorkflowUtils.EMPTY_STRING ) )
        {
            strFieldError = FIELD_WORKFLOW_NAME;
        }
        else if ( ( strDescription == null ) || strDescription.trim(  ).equals( WorkflowUtils.EMPTY_STRING ) )
        {
            strFieldError = FIELD_WORKFLOW_DESCRIPTION;
        }

        if ( !strFieldError.equals( WorkflowUtils.EMPTY_STRING ) )
        {
            Object[] tabRequiredFields = { I18nService.getLocalizedString( strFieldError, getLocale(  ) ) };

            return AdminMessageService.getMessageUrl( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields,
                AdminMessage.TYPE_STOP );
        }

        workflow.setName( strName );
        workflow.setDescription( strDescription );
        workflow.setWorkgroup( strWorkgroup );

        return null;
    }

    /**
     * Gets the workflow creation page
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The workflow creation page
     */
    public String getCreateState( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );
        Workflow workflow = null;

        if ( nIdWorkflow != WorkflowUtils.CONSTANT_ID_NULL )
        {
            workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );
        }

        if ( ( workflow == null ) )
        {
            throw new AccessDeniedException(  );
        }
        
        List<Icon> listIcon = IconHome.getListIcons( getPlugin(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_WORKFLOW, workflow );
        model.put( MARK_INITIAL_STATE, StateHome.getInitialState( nIdWorkflow, getPlugin(  ) ) != null );
        model.put( MARK_ICON_LIST, listIcon );
        setPageTitleProperty( PROPERTY_CREATE_STATE_PAGE_TITLE );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_STATE, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Perform the workflow creation
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doCreateState( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );

        if ( ( request.getParameter( PARAMETER_CANCEL ) == null ) )
        {
            Plugin plugin = getPlugin(  );
            Workflow workflow = null;

            if ( nIdWorkflow != WorkflowUtils.CONSTANT_ID_NULL )
            {
                workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );
            }

            if ( ( workflow == null ) )
            {
                throw new AccessDeniedException(  );
            }

            State state = new State(  );
            state.setWorkflow( workflow );

            String strError = getStateData( request, state, getLocale(  ) );

            if ( strError != null )
            {
                return strError;
            }

            if ( state.isInitialState(  ) )
            {
                // test if initial test already exist
                State stateInitial = StateHome.getInitialState( nIdWorkflow, getPlugin(  ) );

                if ( stateInitial != null )
                {
                    Object[] tabInitialState = { stateInitial.getName(  ) };

                    return AdminMessageService.getMessageUrl( request, MESSAGE_INITIAL_STATE_ALREADY_EXIST,
                        tabInitialState, AdminMessage.TYPE_STOP );
                }
            }

            StateHome.create( state, plugin );
        }

        return getJspModifyWorkflow( request, nIdWorkflow );
    }

    /**
     * Gets the workflow creation page
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The workflow creation page
     */
    public String getModifyState( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdState = request.getParameter( PARAMETER_ID_STATE );
        int nIdState = WorkflowUtils.convertStringToInt( strIdState );
        State state = null;

        if ( nIdState != WorkflowUtils.CONSTANT_ID_NULL )
        {
            state = StateHome.findByPrimaryKey( nIdState, getPlugin(  ) );
        }

        if ( ( state == null ) )
        {
            throw new AccessDeniedException(  );
        }

        List<Icon> listIcon = IconHome.getListIcons( getPlugin(  ) );
        
        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_STATE, state );
        model.put( MARK_ICON_LIST, listIcon );
        setPageTitleProperty( PROPERTY_MODIFY_STATE_PAGE_TITLE );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_STATE, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Perform the workflow modification
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doModifyState( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdState = request.getParameter( PARAMETER_ID_STATE );
        int nIdState = WorkflowUtils.convertStringToInt( strIdState );
        State state = null;

        if ( nIdState != WorkflowUtils.CONSTANT_ID_NULL )
        {
            state = StateHome.findByPrimaryKey( nIdState, getPlugin(  ) );
        }

        if ( ( state == null ) )
        {
            throw new AccessDeniedException(  );
        }

        if ( request.getParameter( PARAMETER_CANCEL ) == null )
        {
            boolean isInitialTestStore = state.isInitialState(  );
            String strError = getStateData( request, state, getLocale(  ) );

            if ( strError != null )
            {
                return strError;
            }

            if ( !isInitialTestStore && state.isInitialState(  ) )
            {
                // test if initial test already exist
                StateFilter filter = new StateFilter(  );
                filter.setIdWorkflow( state.getWorkflow(  ).getId(  ) );
                filter.setIsInitialState( StateFilter.FILTER_TRUE );

                List<State> listState = StateHome.getListStateByFilter( filter, getPlugin(  ) );

                if ( listState.size(  ) != 0 )
                {
                    Object[] tabInitialState = { listState.get( 0 ).getName(  ) };

                    return AdminMessageService.getMessageUrl( request, MESSAGE_INITIAL_STATE_ALREADY_EXIST,
                        tabInitialState, AdminMessage.TYPE_STOP );
                }
            }

            StateHome.update( state, getPlugin(  ) );
        }

        return getJspModifyWorkflow( request, state.getWorkflow(  ).getId(  ) );
    }

    /**
     * Gets the confirmation page of remove all Directory Record
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return the confirmation page of delete all Directory Record
     */
    public String getConfirmRemoveState( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdState = request.getParameter( PARAMETER_ID_STATE );
        int nIdState = WorkflowUtils.convertStringToInt( strIdState );

        ActionFilter filter = new ActionFilter(  );
        filter.setIdStateBefore( nIdState );

        List<Action> listActionStateBefore = ActionHome.getListActionByFilter( filter, getPlugin(  ) );
        filter.setIdStateBefore( ActionFilter.ALL_INT );
        filter.setIdStateAfter( nIdState );

        List<Action> listActionStateAfter = ActionHome.getListActionByFilter( filter, getPlugin(  ) );

        if ( ( listActionStateBefore.size(  ) != 0 ) || ( listActionStateAfter.size(  ) != 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_CAN_NOT_REMOVE_STATE_ACTIONS_ARE_ASSOCIATE,
                AdminMessage.TYPE_STOP );
        }

        UrlItem url = new UrlItem( JSP_DO_REMOVE_STATE );
        url.addParameter( PARAMETER_ID_STATE, strIdState );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_STATE, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Remove all workflow record of the workflow
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doRemoveState( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdState = request.getParameter( PARAMETER_ID_STATE );
        int nIdState = WorkflowUtils.convertStringToInt( strIdState );

        ActionFilter filter = new ActionFilter(  );
        filter.setIdStateBefore( nIdState );

        List<Action> listActionStateBefore = ActionHome.getListActionByFilter( filter, getPlugin(  ) );
        filter.setIdStateBefore( ActionFilter.ALL_INT );
        filter.setIdStateAfter( nIdState );

        List<Action> listActionStateAfter = ActionHome.getListActionByFilter( filter, getPlugin(  ) );

        if ( ( listActionStateBefore.size(  ) != 0 ) || ( listActionStateAfter.size(  ) != 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_CAN_NOT_REMOVE_STATE_ACTIONS_ARE_ASSOCIATE,
                AdminMessage.TYPE_STOP );
        }

        State state = StateHome.findByPrimaryKey( nIdState, getPlugin(  ) );

        if ( state != null )
        {
            StateHome.remove( nIdState, getPlugin(  ) );

            return getJspModifyWorkflow( request, state.getWorkflow(  ).getId(  ) );
        }

        return getJspManageWorkflow( request );
    }

    /**
     * set the data of the  state in the  state object
     * @param request  The HTTP request
     * @param  state the  state object
     * @param locale the locale
     * @return null if no error appear
     */
    private String getStateData( HttpServletRequest request, State state, Locale locale )
    {
        String strName = request.getParameter( PARAMETER_NAME );
        String strDescription = request.getParameter( PARAMETER_DESCRIPTION );
        String strIsInitialState = request.getParameter( PARAMETER_IS_INITIAL_STATE );
        String strIsRequiredWorkgroupAssigned = request.getParameter( PARAMETER_IS_REQUIRED_WORKGROUP_ASSIGNED );
        String strIdIcon = request.getParameter( PARAMETER_ID_ICON );

        String strFieldError = WorkflowUtils.EMPTY_STRING;

        if ( ( strName == null ) || strName.trim(  ).equals( WorkflowUtils.EMPTY_STRING ) )
        {
            strFieldError = FIELD_STATE_NAME;
        }
        else if ( ( strDescription == null ) || strDescription.trim(  ).equals( WorkflowUtils.EMPTY_STRING ) )
        {
            strFieldError = FIELD_STATE_DESCRIPTION;
        }

        if ( !strFieldError.equals( WorkflowUtils.EMPTY_STRING ) )
        {
            Object[] tabRequiredFields = { I18nService.getLocalizedString( strFieldError, getLocale(  ) ) };

            return AdminMessageService.getMessageUrl( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields,
                AdminMessage.TYPE_STOP );
        }
        
        int nIdIcon = WorkflowUtils.convertStringToInt( strIdIcon );

        state.setName( strName );
        state.setDescription( strDescription );
        state.setInitialState( strIsInitialState != null );
        state.setRequiredWorkgroupAssigned( strIsRequiredWorkgroupAssigned != null );
        
        Icon icon = new Icon(  );
        icon.setId( nIdIcon );
        state.setIcon( icon );

        return null;
    }

    /**
     * Gets the workflow creation page
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The workflow creation page
     */
    public String getCreateAction( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );
        Workflow workflow = null;

        if ( nIdWorkflow != WorkflowUtils.CONSTANT_ID_NULL )
        {
            workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );
        }

        if ( ( workflow == null ) )
        {
            throw new AccessDeniedException(  );
        }

        Map<String, Object> model = new HashMap<String, Object>(  );

        StateFilter filter = new StateFilter(  );
        filter.setIdWorkflow( nIdWorkflow );

        List<State> listState = StateHome.getListStateByFilter( filter, getPlugin(  ) );
        List<Icon> listIcon = IconHome.getListIcons( getPlugin(  ) );

        model.put( MARK_WORKFLOW, workflow );
        model.put( MARK_STATE_LIST, WorkflowUtils.getRefList( listState, false, getLocale(  ) ) );
        model.put( MARK_ICON_LIST, listIcon );

        setPageTitleProperty( PROPERTY_CREATE_ACTION_PAGE_TITLE );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_ACTION, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Perform the workflow creation
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doCreateAction( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdWorkflow = request.getParameter( PARAMETER_ID_WORKFLOW );
        int nIdWorkflow = WorkflowUtils.convertStringToInt( strIdWorkflow );

        if ( ( request.getParameter( PARAMETER_CANCEL ) == null ) )
        {
            Plugin plugin = getPlugin(  );
            Workflow workflow = null;

            if ( nIdWorkflow != WorkflowUtils.CONSTANT_ID_NULL )
            {
                workflow = WorkflowHome.findByPrimaryKey( nIdWorkflow, getPlugin(  ) );
            }

            if ( ( workflow == null ) )
            {
                throw new AccessDeniedException(  );
            }

            Action action;
            action = new Action(  );
            action.setWorkflow( workflow );

            String strError = getActionData( request, action, getLocale(  ) );

            if ( strError != null )
            {
                return strError;
            }

            ActionHome.create( action, plugin );

            if ( request.getParameter( PARAMETER_APPLY ) != null )
            {
                return getJspModifyAction( request, action.getId(  ) );
            }
        }

        return getJspModifyWorkflow( request, nIdWorkflow );
    }

    /**
     * set the data of the  action in the  action object
     * @param request The HTTP request
     * @param action the  action object
     * @param locale the locale
     * @return null if no error appear
     */
    private String getActionData( HttpServletRequest request, Action action, Locale locale )
    {
        String strName = request.getParameter( PARAMETER_NAME );
        String strDescription = request.getParameter( PARAMETER_DESCRIPTION );
        String strIdStateBefore = request.getParameter( PARAMETER_ID_STATE_BEFORE );
        String strIdStateAfter = request.getParameter( PARAMETER_ID_STATE_AFTER );
        String strIdIcon = request.getParameter( PARAMETER_ID_ICON );
        String strAutomatic = request.getParameter( PARAMETER_ID_AUTOMATIC );

        int nIdStateBefore = WorkflowUtils.convertStringToInt( strIdStateBefore );
        int nIdStateAfter = WorkflowUtils.convertStringToInt( strIdStateAfter );
        int nIdIcon = WorkflowUtils.convertStringToInt( strIdIcon );

        String strFieldError = WorkflowUtils.EMPTY_STRING;

        if ( ( strName == null ) || strName.trim(  ).equals( WorkflowUtils.EMPTY_STRING ) )
        {
            strFieldError = FIELD_ACTION_NAME;
        }
        else if ( ( strDescription == null ) || strDescription.trim(  ).equals( WorkflowUtils.EMPTY_STRING ) )
        {
            strFieldError = FIELD_ACTION_DESCRIPTION;
        }
        else if ( nIdStateBefore == WorkflowUtils.CONSTANT_ID_NULL )
        {
            strFieldError = FIELD_STATE_BEFORE;
        }
        else if ( nIdStateAfter == WorkflowUtils.CONSTANT_ID_NULL )
        {
            strFieldError = FIELD_STATE_AFTER;
        }
        else if ( nIdIcon == WorkflowUtils.CONSTANT_ID_NULL )
        {
            strFieldError = FIELD_ICON;
        }
        else if ( ( strAutomatic != null ) && ( nIdStateBefore == nIdStateAfter ) )
        {
            Object[] tabRequiredFields = 
                {
                    I18nService.getLocalizedString( FIELD_STATE_BEFORE, getLocale(  ) ),
                    I18nService.getLocalizedString( FIELD_STATE_AFTER, getLocale(  ) )
                };

            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_AUTOMATIC_FIELD, tabRequiredFields,
                AdminMessage.TYPE_STOP );
        }

        if ( !strFieldError.equals( WorkflowUtils.EMPTY_STRING ) )
        {
            Object[] tabRequiredFields = { I18nService.getLocalizedString( strFieldError, getLocale(  ) ) };

            return AdminMessageService.getMessageUrl( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields,
                AdminMessage.TYPE_STOP );
        }

        if ( ( action != null ) && ( strAutomatic != null ) )
        {
            for ( ITask task : TaskHome.getListTaskByIdAction( action.getId(  ), getPlugin(  ), getLocale(  ) ) )
            {
                if ( !task.isTaskForActionAutomatic(  ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_TASK_IS_NOT_AUTOMATIC,
                        AdminMessage.TYPE_STOP );
                }
            }
        }

        action.setName( strName );
        action.setDescription( strDescription );

        State stateBefore = new State(  );
        stateBefore.setId( nIdStateBefore );
        action.setStateBefore( stateBefore );

        State stateAfter = new State(  );
        stateAfter.setId( nIdStateAfter );
        action.setStateAfter( stateAfter );

        Icon icon = new Icon(  );
        icon.setId( nIdIcon );
        action.setIcon( icon );

        action.setAutomaticState( strAutomatic != null );

        return null;
    }

    /**
     * Gets the workflow creation page
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The workflow creation page
     */
    public String getModifyAction( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdAction = request.getParameter( PARAMETER_ID_ACTION );
        int nIdAction = WorkflowUtils.convertStringToInt( strIdAction );
        Action action = null;

        if ( nIdAction != WorkflowUtils.CONSTANT_ID_NULL )
        {
            action = ActionHome.findByPrimaryKey( nIdAction, getPlugin(  ) );
        }

        if ( ( action == null ) )
        {
            throw new AccessDeniedException(  );
        }

        StateFilter filter = new StateFilter(  );
        filter.setIdWorkflow( action.getWorkflow(  ).getId(  ) );

        List<State> listState = StateHome.getListStateByFilter( filter, getPlugin(  ) );
        List<Icon> listIcon = IconHome.getListIcons( getPlugin(  ) );

        ReferenceList refListTaskType = new ReferenceList(  );
        Collection<ITaskType> taskTypeList = WorkflowService.getInstance(  ).getTaskTypeList(  );

        if ( ( action != null ) && action.isAutomaticState(  ) )
        {
            for ( ITaskType taskType : taskTypeList )
            {
                if ( WorkflowService.getInstance(  ).getTaskInstance( taskType.getKey(  ), getLocale(  ) )
                                        .isTaskForActionAutomatic(  ) )
                {
                    refListTaskType.addItem( taskType.getKey(  ),
                        I18nService.getLocalizedString( taskType.getTitleI18nKey(  ), getLocale(  ) ) );
                }
            }
        }
        else
        {
            refListTaskType = WorkflowService.getInstance(  ).getRefListTaskType( getLocale(  ) );
        }

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_ACTION, action );
        setPageTitleProperty( PROPERTY_MODIFY_ACTION_PAGE_TITLE );
        model.put( MARK_STATE_LIST, WorkflowUtils.getRefList( listState, false, getLocale(  ) ) );
        model.put( MARK_TASK_TYPE_LIST, refListTaskType );
        model.put( MARK_TASK_LIST, TaskHome.getListTaskByIdAction( nIdAction, getPlugin(  ), getLocale(  ) ) );
        model.put( MARK_ICON_LIST, listIcon );
        model.put( MARK_PLUGIN, getPlugin(  ) );
        model.put( MARK_LOCALE, getLocale(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_ACTION, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Perform the workflow modification
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doModifyAction( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdAction = request.getParameter( PARAMETER_ID_ACTION );
        int nIdAction = WorkflowUtils.convertStringToInt( strIdAction );
        Action action = null;

        if ( nIdAction != WorkflowUtils.CONSTANT_ID_NULL )
        {
            action = ActionHome.findByPrimaryKey( nIdAction, getPlugin(  ) );
        }

        if ( ( action == null ) )
        {
            throw new AccessDeniedException(  );
        }

        if ( request.getParameter( PARAMETER_CANCEL ) == null )
        {
            String strError = getActionData( request, action, getLocale(  ) );

            if ( strError != null )
            {
                return strError;
            }

            ActionHome.update( action, getPlugin(  ) );
        }

        if ( request.getParameter( PARAMETER_APPLY ) != null )
        {
            return getJspModifyAction( request, nIdAction );
        }

        return getJspModifyWorkflow( request, action.getWorkflow(  ).getId(  ) );
    }

    /**
     * Gets the confirmation page of remove all Directory Record
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return the confirmation page of delete all Directory Record
     */
    public String getConfirmRemoveAction( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdAction = request.getParameter( PARAMETER_ID_ACTION );

        UrlItem url = new UrlItem( JSP_DO_REMOVE_ACTION );
        url.addParameter( PARAMETER_ID_ACTION, strIdAction );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_ACTION, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Remove all workflow record of the workflow
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doRemoveAction( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdAction = request.getParameter( PARAMETER_ID_ACTION );
        int nIdAction = WorkflowUtils.convertStringToInt( strIdAction );
        Action action = ActionHome.findByPrimaryKey( nIdAction, getPlugin(  ) );

        if ( action != null )
        {
            ActionHome.remove( nIdAction, getPlugin(  ) );

            return getJspModifyWorkflow( request, action.getWorkflow(  ).getId(  ) );
        }

        return getJspManageWorkflow( request );
    }

    /**
     * Remove all workflow record of the workflow
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doInsertTask( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdAction = request.getParameter( PARAMETER_ID_ACTION );
        int nIdAction = WorkflowUtils.convertStringToInt( strIdAction );
        String strTaskTypeKey = request.getParameter( PARAMETER_TASK_TYPE_KEY );
        Action action = ActionHome.findByPrimaryKey( nIdAction, getPlugin(  ) );
        ITask task = WorkflowService.getInstance(  ).getTaskInstance( strTaskTypeKey, getLocale(  ) );

        if ( ( action != null ) && ( task != null ) )
        {
            task.setAction( action );
            TaskHome.create( task, getPlugin(  ) );
        }

        return getJspModifyAction( request, nIdAction );
    }

    /**
     * Gets the workflow creation page
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The workflow creation page
     */
    public String getModifyTask( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdTask = request.getParameter( PARAMETER_ID_TASK );
        int nIdTask = WorkflowUtils.convertStringToInt( strIdTask );
        ITask task = TaskHome.findByPrimaryKey( nIdTask, getPlugin(  ), getLocale(  ) );

        if ( ( task == null ) )
        {
            throw new AccessDeniedException(  );
        }

        Map<String, Object> model = new HashMap<String, Object>(  );

        model.put( MARK_TASK, task );
        model.put( MARK_TASK_CONFIG, task.getDisplayConfigForm( request, getPlugin(  ), getLocale(  ) ) );

        setPageTitleProperty( PROPERTY_MODIFY_TASK_PAGE_TITLE );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_TASK, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Modify task
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doModifyTask( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdTask = request.getParameter( PARAMETER_ID_TASK );
        int nIdTask = WorkflowUtils.convertStringToInt( strIdTask );
        ITask task = TaskHome.findByPrimaryKey( nIdTask, getPlugin(  ), getLocale(  ) );

        if ( task != null )
        {
            if ( request.getParameter( PARAMETER_CANCEL ) == null )
            {
                String strError = task.doSaveConfig( request, getLocale(  ), getPlugin(  ) );

                if ( strError != null )
                {
                    return strError;
                }
            }

            if ( request.getParameter( PARAMETER_APPLY ) != null )
            {
                return getJspModifyTask( request, nIdTask );
            }
            else
            {
                return getJspModifyAction( request, task.getAction(  ).getId(  ) );
            }
        }

        return getJspManageWorkflow( request );
    }

    /**
     * Gets the confirmation page of remove task
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return the confirmation page of delete Task
     */
    public String getConfirmRemoveTask( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strId = request.getParameter( PARAMETER_ID_TASK );

        UrlItem url = new UrlItem( JSP_DO_REMOVE_TASK );
        url.addParameter( PARAMETER_ID_TASK, strId );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_TASK, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Remove all workflow record of the workflow
     *
     * @param request
     *            The HTTP request
     * @throws AccessDeniedException
     *             the {@link AccessDeniedException}
     * @return The URL to go after performing the action
     */
    public String doRemoveTask( HttpServletRequest request )
        throws AccessDeniedException
    {
        String strIdTask = request.getParameter( PARAMETER_ID_TASK );
        int nIdTask = WorkflowUtils.convertStringToInt( strIdTask );
        ITask task = TaskHome.findByPrimaryKey( nIdTask, getPlugin(  ), getLocale(  ) );

        if ( task != null )
        {
            List<String> listErrors = new ArrayList<String>(  );

            if ( !TaskRemovalListenerService.getService(  ).checkForRemoval( strIdTask, listErrors, getLocale(  ) ) )
            {
                String strCause = AdminMessageService.getFormattedList( listErrors, getLocale(  ) );
                Object[] arguments = { strCause };

                return AdminMessageService.getMessageUrl( request, MESSAGE_CAN_NOT_REMOVE_TASK, arguments,
                    AdminMessage.TYPE_STOP );
            }

            if ( task.isConfigRequire(  ) )
            {
                task.doRemoveConfig( getPlugin(  ) );
            }

            TaskHome.remove( nIdTask, getPlugin(  ) );

            Action action = ActionHome.findByPrimaryKey( task.getAction(  ).getId(  ), getPlugin(  ) );

            if ( action != null )
            {
                return getJspModifyWorkflow( request, action.getWorkflow(  ).getId(  ) );
            }
        }

        return getJspManageWorkflow( request );
    }

    /**
     * return a reference list wich contains the different state of a workflow
     *
     * @param locale
     *            the locale
     * @return reference list of workflow state
     */
    private ReferenceList getRefListActive( Locale locale )
    {
        ReferenceList refListState = new ReferenceList(  );
        String strAll = I18nService.getLocalizedString( PROPERTY_ALL, locale );
        String strYes = I18nService.getLocalizedString( PROPERTY_YES, locale );
        String strNo = I18nService.getLocalizedString( PROPERTY_NO, locale );

        refListState.addItem( -1, strAll );
        refListState.addItem( 1, strYes );
        refListState.addItem( 0, strNo );

        return refListState;
    }

    /**
     * return url of the jsp modify workflow
     *
     * @param request
     *            The HTTP request
     * @param nIdWorkflow
     *            the key of workflow to modify
     * @return return url of the jsp modify workflows
     */
    private String getJspModifyWorkflow( HttpServletRequest request, int nIdWorkflow )
    {
        return AppPathService.getBaseUrl( request ) + JSP_MODIFY_WORKFLOW + "?" + PARAMETER_ID_WORKFLOW + "=" +
        nIdWorkflow;
    }

    /**
     * return url of the jsp modify action
     *
     * @param request
     *            The HTTP request
     * @param nIdAction
     *            the key of action to modify
     * @return return url of the jsp modify action
     */
    private String getJspModifyTask( HttpServletRequest request, int nIdTask )
    {
        return AppPathService.getBaseUrl( request ) + JSP_MODIFY_TASK + "?" + PARAMETER_ID_TASK + "=" + nIdTask;
    }

    /**
     * return url of the jsp modify action
     *
     * @param request
     *            The HTTP request
     * @param nIdAction
     *            the key of action to modify
     * @return return url of the jsp modify action
     */
    private String getJspModifyAction( HttpServletRequest request, int nIdAction )
    {
        return AppPathService.getBaseUrl( request ) + JSP_MODIFY_ACTION + "?" + PARAMETER_ID_ACTION + "=" + nIdAction;
    }

    /**
     * return url of the jsp manage workflow
     * @param request The HTTP request
     * @return url of the jsp manage workflow
     */
    private String getJspManageWorkflow( HttpServletRequest request )
    {
        return AppPathService.getBaseUrl( request ) + JSP_MANAGE_WORKFLOW;
    }

    /**
     * Returns advanced parameters form
     *
     * @param request The Http request
     * @return Html form
     */
    public String getManageAdvancedParameters( HttpServletRequest request )
    {
        if ( !RBACService.isAuthorized( Action.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                    ActionResourceIdService.PERMISSION_MANAGE_ADVANCED_PARAMETERS, getUser(  ) ) )
        {
            return getManageWorkflow( request );
        }

        Map<String, Object> model = WorkflowService.getInstance(  ).getManageAdvancedParameters( getUser(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_ADVANCED_PARAMETERS, getLocale(  ),
                model );

        return getAdminPage( template.getHtml(  ) );
    }
}