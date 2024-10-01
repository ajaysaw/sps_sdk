package com.lib.sps

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.lib.sps.model.EkycDeviceList
import com.google.gson.internal.LinkedTreeMap
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class BiometricUtils {
    private lateinit var deviceDetails: EkycDeviceList
    private lateinit var biometricFormat: String
    private var wadh: String = ""
    private lateinit var pidBlockNodes: LinkedTreeMap<String, Any>
    private lateinit var context: Context

    fun callCapture(
        deviceDetails: EkycDeviceList,
        biometricFormat: String,
        wadh: String,
        pidBlockNodes: LinkedTreeMap<String, Any>,
        context:Context,
    ) :BiometricActionData {
        this.deviceDetails = deviceDetails
        this.biometricFormat = biometricFormat
        this.wadh = wadh
        this.pidBlockNodes = pidBlockNodes
        this.context = context
        try {
            if (deviceDetails.isBiometric == true) {
                if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("morpho", ignoreCase = true)) {
                    if (searchPackageName("morpho", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.scl.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("startek", ignoreCase = true)) {
                    if (searchPackageName("startek", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.acpl.registersdk",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("mantra", ignoreCase = true)) {
                    if (searchPackageName("mantra", deviceDetails.isBiometric!!)
                    ) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.mantra.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("secugen", ignoreCase = true)) {
                    if (searchPackageName("secugen", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.secugen.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("precision", ignoreCase = true)) {
                    if (searchPackageName("precision", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPrecisionPidOptXML(biometricFormat, pidBlockNodes)
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.precision.pb510.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("identi5", ignoreCase = true)) {
                    if (searchPackageName("identi5", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPrecisionPidOptXML(biometricFormat, pidBlockNodes)
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.evolute.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    }else{
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("Mantra L1", ignoreCase = true)) {
                    if (searchPackageName("Mantra L1",deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.mantra.mfs110.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("Morpho L1", ignoreCase = true)) {
                    if (searchPackageName("Morpho L1", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.idemia.l1rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("Startek L1", ignoreCase = true)) {
                    if (searchPackageName("Startek L1", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.acpl.registersdk_l1",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("PB1000 L1", ignoreCase = true)) {
                    if (searchPackageName("PB1000 L1", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","in.co.precisionit.innaitaadhaar",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("Tatvik", ignoreCase = true)) {
                    if (searchPackageName("Tatvik", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.tatvik.bio.tmf20",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                } else if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("Identi5 L1", ignoreCase = true)) {
                    if (searchPackageName("Identi5 L1", deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE","com.evolute.A600.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    } else {
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                }else{
                    if (searchPackageName(deviceDetails.deviceCode.toString(), deviceDetails.isBiometric!!)) {
                        val pidOptXML: String = createPidOptXML(biometricFormat, wadh, pidBlockNodes)!!
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE",deviceDetails.packageName.toString(),pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    }else{
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                }
            } else {
                if (deviceDetails.deviceCode?.trim { it <= ' ' }.equals("mantra", ignoreCase = true)) {
                        val fType = if (pidBlockNodes.containsKey("fType")) {
                            pidBlockNodes["fType"].toString()
                        } else {
                            "0"
                        }
                        val pidOptXML: String = createIrisPidOptXML(biometricFormat, wadh, fType)
                        return  BiometricActionData("in.gov.uidai.rdservice.iris.CAPTURE","com.mantra.mis100v2.rdservice",pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                }else{
                    if (searchPackageName(deviceDetails.deviceCode.toString(), deviceDetails.isBiometric!!)) {
                        val fType = if (pidBlockNodes.containsKey("fType")) {
                            pidBlockNodes["fType"].toString()
                        } else {
                            "0"
                        }
                        val pidOptXML: String = createIrisPidOptXML(biometricFormat, wadh, fType)
                        return  BiometricActionData("in.gov.uidai.rdservice.fp.CAPTURE",deviceDetails.packageName.toString(),pidOptXML,false, errorMessage = deviceDetails.errorMessage!!)
                    }else{
                        return  BiometricActionData("","","",true, errorMessage = deviceDetails.errorMessage!!)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("exception", "e")
            return  BiometricActionData("","","",true,"Error")
        }
        return  BiometricActionData("","","",true,"Error")
    }

    private fun searchPackageName(deviceName: String, isBiometricSelected: Boolean): Boolean {
        var string = ""
        var message = ""
        if (isBiometricSelected) {
            when {
                deviceName.equals("morpho", ignoreCase = true) -> {
                    string = "com.scl.rdservice"
                    message = "Please install `Morpho SCL RDService` App."
                }
                deviceName.equals("startek", ignoreCase = true) -> {
                    string = "com.acpl.registersdk"
                    message = "Please install `ACPL FM220 Registered Device` Service."
                }
                deviceName.equals("mantra", ignoreCase = true) -> {
                    string = "com.mantra.rdservice"
                    message = "Please install `Mantra RDService` App."
                }
                deviceName.equals("secugen", ignoreCase = true) -> {
                    string = "com.secugen.rdservice"
                    message = "Please install `SecuGen RD Service` App."
                }
                deviceName.equals("precision", ignoreCase = true) -> {
                    string = "com.precision.pb510.rdservice"
                    message = "Please install `PB510 RDService` App."
                }
                deviceName.equals("identi5", ignoreCase = true) -> {
                    string = "com.evolute.rdservice"
                    message = "Please install `Evolute RD Service` App."
                }
                deviceName.equals("Mantra L1", ignoreCase = true) -> {
                    string = "com.mantra.mfs110.rdservice"
                    message = "Please install Mantra L1 RD Service App."
                }
                deviceName.equals("Morpho L1", ignoreCase = true) -> {
                    string = "com.idemia.l1rdservice"
                    message = "Please install Morpho L1 RD Service App."
                }
                deviceName.equals("Startek L1", ignoreCase = true) -> {
                    string = "com.acpl.registersdk_l1"
                    message = "Please install Startek L1 RD Service App."
                }
                deviceName.equals("PB1000 L1", ignoreCase = true) -> {
                    string = "in.co.precisionit.innaitaadhaar"
                    message = "Please install PB1000 L1 RD Service App."
                }
                deviceName.equals("Tatvik", ignoreCase = true) -> {
                    string = "com.tatvik.bio.tmf20"
                    message = "Please install `Tatvik RD Service` App."
                }
                deviceName.equals("Identi5 L1", ignoreCase = true) -> {
                    string = "com.evolute.A600.rdservice"
                    message = "Please install `Evolute L1 RD Service` App."
                }else->{
                    string = deviceDetails.packageName.toString()
                    message = deviceDetails.errorMessage.toString()
                }
            }
        } else {
            if (deviceName.equals("mantra", ignoreCase = true)) {
                string = "com.mantra.mis100v2.rdservice"
                message = "Please install `Mantra RDService` App."
            }else{
                string = deviceDetails.packageName.toString()
                message = deviceDetails.errorMessage.toString()
            }
        }
        val packageName = string
        return if (!isPackageExisted(packageName)) {
            AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Message")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    val intentPlay = Intent(Intent.ACTION_VIEW)
                    //intentPlay.setData(Uri.parse("market://details?id=com.acpl.registersdk"));
                    intentPlay.data = Uri.parse("market://details?id=$packageName") // com.scl.rdservice"));
                    context.startActivity(intentPlay)
                    dialog.cancel()
                }
                .show()
            false
        } else true
    }

    private fun isPackageExisted(targetPackage: String): Boolean {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList = context.packageManager.queryIntentActivities(mainIntent, 0)
        for (packageInfo in pkgAppsList) {
            if (packageInfo.activityInfo.processName == targetPackage) return true
        }
        return false
    }

    private fun createPrecisionPidOptXML(biometricFormat: String, pidBlockNodes: LinkedTreeMap<String, Any>): String {
        var tmpOptXml = ""
        return try {
            val docFactory = DocumentBuilderFactory.newInstance()
            docFactory.isNamespaceAware = true
            var docBuilder: DocumentBuilder? = null
            docBuilder = docFactory.newDocumentBuilder()
            val doc = docBuilder.newDocument()
            doc.xmlStandalone = true
            val rootElement = doc.createElement("PidOptions")
            doc.appendChild(rootElement)
            val attrs = doc.createAttribute("ver")
            attrs.value = if (pidBlockNodes.containsKey("ver")) {
                pidBlockNodes["ver"].toString()
            } else {
                "1.0"
            }
            rootElement.setAttributeNode(attrs)
            val opts = doc.createElement("Opts")
            rootElement.appendChild(opts)
            var attr = doc.createAttribute("fCount")
            attr.value = if (pidBlockNodes.containsKey("fCount")) {
                pidBlockNodes["fCount"].toString()
            } else {
                "1"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("fType")
            attr.value = if (pidBlockNodes.containsKey("fType")) {
                pidBlockNodes["fType"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("iCount")
            attr.value = if (pidBlockNodes.containsKey("iCount")) {
                pidBlockNodes["iCount"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("iType")
            attr.value = if (pidBlockNodes.containsKey("iType")) {
                pidBlockNodes["iType"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pCount")
            attr.value = if (pidBlockNodes.containsKey("pCount")) {
                pidBlockNodes["pCount"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pType")
            attr.value = if (pidBlockNodes.containsKey("pType")) {
                pidBlockNodes["pType"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("format")
            attr.value = biometricFormat.ifEmpty { "1" }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pidVer")
            attr.value = if (pidBlockNodes.containsKey("pidVer")) {
                pidBlockNodes["pidVer"].toString()
            } else {
                "2.0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("timeout")
            attr.value = if (pidBlockNodes.containsKey("timeout")) {
                pidBlockNodes["timeout"].toString()
            } else {
                "10000"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("otp")
            attr.value = if (pidBlockNodes.containsKey("otp")) {
                pidBlockNodes["otp"].toString()
            } else {
                ""
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("env")
            attr.value = if (pidBlockNodes.containsKey("env")) {
                pidBlockNodes["env"].toString()
            } else {
                "P"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("wadh")
            //attr.setValue("ONLY USE FOR E-KYC.");
            attr.value = if (pidBlockNodes.containsKey("wadh")) {
                pidBlockNodes["wadh"].toString()
            } else {
                ""
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("posh")
            attr.value = if (pidBlockNodes.containsKey("posh")) {
                pidBlockNodes["posh"].toString()
            } else {
                "UNKNOWN"
            }
            opts.setAttributeNode(attr)
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            val source = DOMSource(doc)
            val writer = StringWriter()
            val result = StreamResult(writer)
            transformer.transform(source, result)
            tmpOptXml = writer.buffer.toString().replace("\n|\r".toRegex(), "")
            tmpOptXml = tmpOptXml.replace("&lt;".toRegex(), "<").replace("&gt;".toRegex(), ">")
            Log.e("Precision Pid Options", tmpOptXml)
            tmpOptXml
        } catch (ex: Exception) {
            //showMessageDialogue(this, "EXCEPTION- " + ex.getMessage(), "EXCEPTION");
            ""
        }
    }

    protected open fun createPidOptXML(biometricFormat: String, wadh: String, pidBlockNodes: LinkedTreeMap<String, Any>): String? {
        var tmpOptXml = ""
        return try {
            val docFactory = DocumentBuilderFactory.newInstance()
            docFactory.isNamespaceAware = true
            var docBuilder: DocumentBuilder? = null
            docBuilder = docFactory.newDocumentBuilder()
            val doc = docBuilder.newDocument()
            doc.xmlStandalone = true
            val rootElement = doc.createElement("PidOptions")
            doc.appendChild(rootElement)
            val attrs = doc.createAttribute("ver")
            attrs.value = if (pidBlockNodes.containsKey("ver")) {
                pidBlockNodes["ver"].toString()
            } else {
                "1.0"
            }
            rootElement.setAttributeNode(attrs)
            val opts = doc.createElement("Opts")
            rootElement.appendChild(opts)
            var attr = doc.createAttribute("fCount")
            attr.value = if (pidBlockNodes.containsKey("fCount")) {
                pidBlockNodes["fCount"].toString()
            } else {
                "1"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("fType")
            attr.value = if (pidBlockNodes.containsKey("fType")) {
                pidBlockNodes["fType"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("iCount")
            attr.value = if (pidBlockNodes.containsKey("iCount")) {
                pidBlockNodes["iCount"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("iType")
            attr.value = if (pidBlockNodes.containsKey("iType")) {
                pidBlockNodes["iType"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pCount")
            attr.value = if (pidBlockNodes.containsKey("pCount")) {
                pidBlockNodes["pCount"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pType")
            attr.value = if (pidBlockNodes.containsKey("pType")) {
                pidBlockNodes["pType"].toString()
            } else {
                "0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("format")
            attr.value = biometricFormat.ifEmpty {
                "1"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pidVer")
            attr.value = if (pidBlockNodes.containsKey("pidVer")) {
                pidBlockNodes["pidVer"].toString()
            } else {
                "2.0"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("timeout")
            attr.value = if (pidBlockNodes.containsKey("timeout")) {
                pidBlockNodes["timeout"].toString()
            } else {
                "10000"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("otp")
            attr.value = if (pidBlockNodes.containsKey("otp")) {
                pidBlockNodes["otp"].toString()
            } else {
                ""
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("env")
            attr.value = if (pidBlockNodes.containsKey("env")) {
                pidBlockNodes["env"].toString()
            } else {
                "P"
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("wadh")
            //attr.setValue("ONLY USE FOR E-KYC.");
            attr.value = wadh.ifEmpty {
                if (pidBlockNodes.containsKey("wadh")) {
                    pidBlockNodes["wadh"].toString()
                } else {
                    ""
                }
            }
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("posh")
            attr.value = if (pidBlockNodes.containsKey("posh")) {
                pidBlockNodes["posh"].toString()
            } else {
                "UNKNOWN"
            }
            opts.setAttributeNode(attr)
            val demo = doc.createElement("Demo")
            demo.textContent = ""
            rootElement.appendChild(demo)
            val custotp = doc.createElement("CustOpts")
            rootElement.appendChild(custotp)
            val param = doc.createElement("Param")
            custotp.appendChild(param)
            attr = doc.createAttribute("name")
            attr.value = "ValidationKey"
            param.setAttributeNode(attr)
            attr = doc.createAttribute("value")
            attr.value = "ONLY USE FOR LOCKED DEVICES."
            param.setAttributeNode(attr)
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")
            val source = DOMSource(doc)
            val writer = StringWriter()
            val result = StreamResult(writer)
            transformer.transform(source, result)
            tmpOptXml = writer.buffer.toString().replace("\n|\r".toRegex(), "")
            tmpOptXml = tmpOptXml.replace("&lt;".toRegex(), "<").replace("&gt;".toRegex(), ">")
            Log.e("Pid Options", tmpOptXml)
            tmpOptXml
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    /////////////////////////////////////////////// PID Options for IRIS ///////////////////////////////////////////////////
    private fun createIrisPidOptXML(biometricFormat: String, wadh: String, fTypeStr: String): String {
        var tmpOptXml = ""
        return try {
            val formatStr: String = if (biometricFormat.isNotEmpty()) {
                biometricFormat
            } else {
                "1"
            }
            val timeOutStr = "10000"
            val envStr: String = "P"
            val docFactory = DocumentBuilderFactory.newInstance()
            docFactory.isNamespaceAware = true
            var docBuilder: DocumentBuilder? = null
            docBuilder = docFactory.newDocumentBuilder()
            val doc = docBuilder.newDocument()
            doc.xmlStandalone = true
            val rootElement = doc.createElement("PidOptions")
            doc.appendChild(rootElement)
            val attrs = doc.createAttribute("ver")
            attrs.value = "1.0"
            rootElement.setAttributeNode(attrs)
            val opts = doc.createElement("Opts")
            rootElement.appendChild(opts)
            var attr = doc.createAttribute("fCount")
            attr.value = "0"
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("fType")
            attr.value = fTypeStr
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("iCount")
            attr.value = "1"
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pCount")
            attr.value = "0"
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("format")
            attr.value = formatStr
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("pidVer")
            attr.value = "2.0"
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("timeout")
            attr.value = timeOutStr
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("posh")
            attr.value = "UNKNOWN"
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("env")
            attr.value = envStr
            opts.setAttributeNode(attr)
            attr = doc.createAttribute("wadh")
            //attr.setValue("ONLY USE FOR E-KYC.");
            if (wadh.isNotEmpty())
                attr.value = wadh
            else
                attr.value = ""
            opts.setAttributeNode(attr)
            val custotp = doc.createElement("CustOpts")
            rootElement.appendChild(custotp)
            val param = doc.createElement("Param")
            custotp.appendChild(param)
            attr = doc.createAttribute("name")
            attr.value = "mantrakey"
            param.setAttributeNode(attr)
            attr = doc.createAttribute("value")
            attr.value = ""
            param.setAttributeNode(attr)
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")
            val source = DOMSource(doc)
            val writer = StringWriter()
            val result = StreamResult(writer)
            transformer.transform(source, result)
            tmpOptXml = writer.buffer.toString().replace("\n|\r".toRegex(), "")
            tmpOptXml = tmpOptXml.replace("&lt;".toRegex(), "<").replace("&gt;".toRegex(), ">")
            Log.e("Pid Options", tmpOptXml)
            tmpOptXml
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

}

class BiometricActionData{
    var action:String = ""
    var packageName:String = ""
    var pidOptXML:String = ""
    var isError:Boolean = false
    var errorMessage: String = ""
    constructor(action:String,packageName:String,pidOptXML:String,isError:Boolean,errorMessage: String){
        this.action = action
        this.packageName = packageName
        this.pidOptXML = pidOptXML
        this.isError = isError
        this.errorMessage = errorMessage
    }
}
