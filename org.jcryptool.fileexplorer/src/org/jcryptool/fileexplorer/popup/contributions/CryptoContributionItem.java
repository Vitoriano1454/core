//-----BEGIN DISCLAIMER-----
/*******************************************************************************
 * Copyright (c) 2011, 2014 JCrypTool Team and Contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
//-----END DISCLAIMER-----
package org.jcryptool.fileexplorer.popup.contributions;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.jcryptool.core.ApplicationActionBarAdvisor;
import org.jcryptool.core.logging.utils.LogUtil;
import org.jcryptool.core.operations.CommandOrAction;
import org.jcryptool.core.operations.algorithm.ShadowAlgorithmHandler;
import org.jcryptool.core.operations.OperationsPlugin;
import org.jcryptool.fileexplorer.FileExplorerPlugin;
import org.jcryptool.fileexplorer.views.FileExplorerView;

/**
 * Cryptographic contribution item. Generates the <b>Encryption/Decryption</b> context menu
 * in the <b>File Explorer</b> view context menu. This menu is dynamically filled with all
 * installed cryptographic algorithms that do extend the <code>AlgorithmsManager</code>.
 *
 * @author Dominik Schadow
 * @author Holger Friedrich (support for Commands)
 * @version 0.9.6
 */
public class CryptoContributionItem extends ContributionItem {
    private Menu algorithmsMenu;
    private final FileExplorerView view;
    private IHandler handler;

    public CryptoContributionItem(FileExplorerView view, IHandler handler) {
        this.view = view;
        this.handler = handler;
    }

    public void fill(Menu menu, int index) {
        algorithmsMenu = new Menu(menu);
        Comparator<String> menuStringsComparator = new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        };

        SortedMap<String, Menu> typeMap = new TreeMap<String, Menu>(menuStringsComparator);
        SortedMap<String, HashMap<String, CommandOrAction>> actionMap = new TreeMap<String, HashMap<String, CommandOrAction>>(menuStringsComparator);
        CommandOrAction[] algorithmActions = OperationsPlugin.getDefault().getAlgorithmsManager().getShadowAlgorithmActions();

        for (final CommandOrAction cmdOrAction : algorithmActions) {
            String translatedType = ApplicationActionBarAdvisor.getTypeTranslation(OperationsPlugin.getDefault().getAlgorithmsManager().getAlgorithmType(cmdOrAction));

            if (!typeMap.containsKey(translatedType)) {
                typeMap.put(translatedType, new Menu(algorithmsMenu));
            }

            HashMap<String, CommandOrAction> map = new HashMap<String, CommandOrAction>(1);
            map.put(translatedType, cmdOrAction);

            String text = null;
            if(cmdOrAction.getHandler() != null) {
            	text = ((ShadowAlgorithmHandler)cmdOrAction.getHandler()).getText();
            } else if(cmdOrAction.getAction() != null) {
            	text = cmdOrAction.getAction().getText();
            }
            actionMap.put(text, map);
        }

        for (String subMenuKey : typeMap.keySet()) {
            MenuItem item = new MenuItem(algorithmsMenu, SWT.CASCADE);
            item.setText(subMenuKey);
            item.setMenu(typeMap.get(subMenuKey));
        }

        for (HashMap<String, CommandOrAction> algorithmItems : actionMap.values()) {
            String translatedType = algorithmItems.keySet().iterator().next();
            final CommandOrAction cmdOrAction = algorithmItems.get(translatedType);

            // get the menu
            Menu typeMenu = typeMap.get(translatedType);

            // create an item for the algorithm
            MenuItem item = new MenuItem(typeMenu, SWT.CASCADE);
            final IAction action = cmdOrAction.getAction();
            final String commandId = cmdOrAction.getCommandId();
            final ShadowAlgorithmHandler handler = (ShadowAlgorithmHandler)cmdOrAction.getHandler();
            
            if(commandId != null) {
            	item.setText(handler.getText());
            	item.addSelectionListener(new SelectionAdapter() {
            		public void widgetSelected(SelectionEvent e) {
            			run(commandId);
            		}
            	});
            } else if(action != null) {
            	item.setText(action.getText());
            	item.addSelectionListener(new SelectionAdapter() {
            		public void widgetSelected(SelectionEvent e) {
            			run(action);
            		}
            	});
            }

            // update the menu
            typeMap.put(translatedType, typeMenu);
        }

        MenuItem item = new MenuItem(menu, SWT.CASCADE, index);
        item.setText(Messages.CryptoContributionItem_0);
        item.setMenu(algorithmsMenu);
    }

    public void run(IAction cryptoAction) {
        final IHandlerService handlerService = (IHandlerService) view.getSite().getService(IHandlerService.class);
        IEvaluationContext evaluationContext = handlerService.createContextSnapshot(true);
        ExecutionEvent event = new ExecutionEvent(null, Collections.EMPTY_MAP, null, evaluationContext);

        try {
            handler.execute(event);
            cryptoAction.run();
        } catch (ExecutionException ex) {
            LogUtil.logError(FileExplorerPlugin.PLUGIN_ID, ex);
        }
    }

    public void run(String commandId) {
        final IHandlerService handlerService = (IHandlerService) view.getSite().getService(IHandlerService.class);
        IEvaluationContext evaluationContext = handlerService.createContextSnapshot(true);
        ExecutionEvent event = new ExecutionEvent(null, Collections.EMPTY_MAP, null, evaluationContext);

        final ICommandService commandService = (ICommandService)view.getSite().getService(ICommandService.class);
        Command command = commandService.getCommand(commandId);
        
        try {
            handler.execute(event);
            command.executeWithChecks(event);
        } catch (Exception ex) {
            LogUtil.logError(FileExplorerPlugin.PLUGIN_ID, ex);
        }
    }
}
