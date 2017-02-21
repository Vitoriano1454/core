//-----BEGIN DISCLAIMER-----
/*******************************************************************************
* Copyright (c) 2017 JCrypTool Team and Contributors
* 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
//-----END DISCLAIMER-----
package org.jcryptool.crypto.flexiprovider.operations.engines;

import org.jcryptool.crypto.flexiprovider.descriptors.IFlexiProviderOperation;

public interface IPerfomOperationListener {

	public void performOperation(IFlexiProviderOperation operation);
	
}
