/*
 * Copyright 2013, 2014 Megion Research and Development GmbH
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.mycelium.wallet;

import android.util.Log;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.io.Files;
import com.mrd.bitlib.model.Address;
import com.mrd.bitlib.util.Sha256Hash;
import com.mrd.bitlib.util.StringUtils;
import com.mycelium.wallet.persistence.MetadataStorage;
import com.mycelium.wapi.model.TransactionEx;
import com.mycelium.wapi.wallet.WalletAccount;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mycelium.wallet.Constants.TAG;

public class ImportElectrumLabels {
   static InputStream is = null;
   static JSONObject jObj = null;

   public static void importElectrum(WalletAccount account, MetadataStorage storage, File file) throws IOException, JSONException {
      List<String> lines = Files.readLines(file, Charset.forName("UTF-8"));
      StringBuilder json = new StringBuilder();
      for (String line : lines) {
         json.append(line);
      }

      JSONObject history = new JSONObject(json.toString());
      Iterator<String> keys = history.keys();
      while (keys.hasNext()){
         String key   = keys.next();
         String label = (String)history.get(key);

         if (Strings.isNullOrEmpty(key)) {
            Log.w(TAG, "importElectrum: Skipping record, key is empty");
            continue;
         }


         // TX ID have 64 chars
         if (key.length() == 64) {
            Sha256Hash tx = Sha256Hash.fromString(key);

            // Skip TX which is not from account, do not mess storage
            if (account.getTransaction(tx) == null) {
               Log.d(TAG, "importElectrum: TXID: " + key + " Label: " + label + " -  Skipping");
               continue;
            }
            Log.d(TAG, "importElectrum: TXID: " + key + " Label: " + label);
            storage.storeTransactionLabel(tx, label);
         }
         // Else assume Address
         else {
            Log.d(TAG, "importElectrum: Address: " + key + " Label: " + label);
            storage.storeAddressLabel(Address.fromString(key), label);
         }
      }
   }
}
