package com.nexus.terminal.core.python

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class PythonPackage(
    val name: String,
    val version: String,
    val summary: String,
    val isInstalled: Boolean
)

/**
 * Package manager for Python libraries using pip and PyPI REST API.
 */
class PipManager(
    private val pythonEngine: PythonEngine,
    private val sitePackagesDir: File
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun installPackage(
        packageName: String,
        onLog: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val pythonBin = pythonEngine.detectPythonBinary()
        val cleanPkg = packageName.trim()
        if (cleanPkg.isEmpty()) return@withContext false

        onLog("[pip] Iniciando instalação de '$cleanPkg'...\n")
        onLog("[pip] Destino: ${sitePackagesDir.absolutePath}\n")

        val command = listOf(
            pythonBin,
            "-m",
            "pip",
            "install",
            cleanPkg,
            "--target",
            sitePackagesDir.absolutePath,
            "--no-warn-script-location"
        )

        try {
            val pb = ProcessBuilder(command)
            pb.redirectErrorStream(true)
            val env = pb.environment()
            env["PYTHONPATH"] = sitePackagesDir.absolutePath

            val proc = pb.start()
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                onLog("$line\n")
            }
            val exitCode = proc.waitFor()
            if (exitCode == 0) {
                onLog("\n[pip] ✓ Pacote '$cleanPkg' instalado com sucesso!\n")
                true
            } else {
                onLog("\n[pip] ✗ Falha na instalação de '$cleanPkg' (código: $exitCode)\n")
                false
            }
        } catch (e: Exception) {
            onLog("\n[pip] Erro: ${e.message}\n")
            false
        }
    }

    suspend fun uninstallPackage(
        packageName: String,
        onLog: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        onLog("[pip] Removendo pacote '$packageName'...\n")
        var removed = false

        val matchingFiles = sitePackagesDir.listFiles { _, name ->
            name.equals(packageName, ignoreCase = true) ||
            name.startsWith("${packageName}-", ignoreCase = true) ||
            name.startsWith("${packageName}_", ignoreCase = true)
        } ?: emptyArray()

        for (file in matchingFiles) {
            try {
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
                removed = true
                onLog("[pip] Removido: ${file.name}\n")
            } catch (e: Exception) {
                onLog("[pip] Falha ao deletar ${file.name}: ${e.message}\n")
            }
        }

        if (removed) {
            onLog("[pip] ✓ Pacote '$packageName' desinstalado.\n")
        } else {
            onLog("[pip] Pacote '$packageName' não encontrado no diretório local.\n")
        }
        removed
    }

    suspend fun searchPyPi(query: String): PythonPackage? = withContext(Dispatchers.IO) {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return@withContext null

        val url = "https://pypi.org/pypi/$clean/json"
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val info = json.getJSONObject("info")
                val name = info.optString("name", clean)
                val version = info.optString("version", "1.0.0")
                val summary = info.optString("summary", "Biblioteca Python do PyPI")

                PythonPackage(
                    name = name,
                    version = version,
                    summary = summary,
                    isInstalled = isPackageInstalled(name)
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun listInstalledPackages(): List<PythonPackage> {
        val packages = mutableListOf<PythonPackage>()
        val files = sitePackagesDir.listFiles() ?: return packages

        for (file in files) {
            if (file.isDirectory && file.name.endsWith(".dist-info")) {
                val metadataFile = File(file, "METADATA")
                if (metadataFile.exists()) {
                    var pkgName = ""
                    var pkgVersion = ""
                    var pkgSummary = ""

                    metadataFile.forEachLine { line ->
                        if (line.startsWith("Name: ")) {
                            pkgName = line.removePrefix("Name: ").trim()
                        } else if (line.startsWith("Version: ")) {
                            pkgVersion = line.removePrefix("Version: ").trim()
                        } else if (line.startsWith("Summary: ")) {
                            pkgSummary = line.removePrefix("Summary: ").trim()
                        }
                    }

                    if (pkgName.isNotEmpty()) {
                        packages.add(
                            PythonPackage(
                                name = pkgName,
                                version = pkgVersion,
                                summary = if (pkgSummary.isNotEmpty()) pkgSummary else "Instalado no ambiente Nexus",
                                isInstalled = true
                            )
                        )
                    }
                }
            }
        }
        return packages.sortedBy { it.name.lowercase() }
    }

    fun isPackageInstalled(packageName: String): Boolean {
        val files = sitePackagesDir.listFiles() ?: return false
        return files.any {
            it.name.startsWith(packageName, ignoreCase = true)
        }
    }
}
