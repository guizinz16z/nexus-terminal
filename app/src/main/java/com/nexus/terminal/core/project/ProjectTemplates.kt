package com.nexus.terminal.core.project

/**
 * Production-ready templates for the Nexus Terminal Project System.
 */
object ProjectTemplates {

    fun getFilesForTemplate(template: ProjectTemplateType, projectName: String): Map<String, String> {
        return when (template) {
            ProjectTemplateType.TELEGRAM_BOT -> mapOf(
                "main.py" to """
                    # -*- coding: utf-8 -*-
                    \"\"\"
                    Template de Bot Python para Nexus Terminal
                    Configurado para automação e comandos interativos.
                    \"\"\"
                    import os
                    import sys
                    import time

                    BOT_NAME = "$projectName"
                    print(f"★ [{BOT_NAME}] Inicializando engine do Bot no Nexus Terminal...")

                    def handle_command(cmd: str):
                        cmd = cmd.strip().lower()
                        if cmd == "/start":
                            return "Olá! O bot está ativo no Nexus Terminal."
                        elif cmd == "/status":
                            return f"Status: Operacional | Python {sys.version.split()[0]}"
                        elif cmd == "/help":
                            return "Comandos disponíveis: /start, /status, /help, /exit"
                        elif cmd == "/exit":
                            return "Encerrando..."
                        return f"Comando desconhecido: '{cmd}'"

                    def main():
                        print(f"✓ Bot '{BOT_NAME}' iniciado com sucesso!")
                        print("➜ Digite um comando para simular (ex: /start, /status, /help, /exit):")
                        try:
                            while True:
                                user_input = input("bot> ")
                                if not user_input:
                                    continue
                                response = handle_command(user_input)
                                print(f"[{BOT_NAME}]: {response}")
                                if user_input.strip() == "/exit":
                                    break
                        except (KeyboardInterrupt, EOFError):
                            print("\n[Bot finalizado]")

                    if __name__ == "__main__":
                        main()
                """.trimIndent(),
                "requirements.txt" to "requests>=2.31.0\npython-dotenv>=1.0.0\n",
                "README.md" to "# $projectName\n\nBot desenvolvido no NEXUS TERMINAL.\n"
            )

            ProjectTemplateType.FASTAPI_SERVER -> mapOf(
                "main.py" to """
                    # -*- coding: utf-8 -*-
                    \"\"\"
                    Servidor HTTP REST API leve para Android / Nexus Terminal
                    Funciona imediatamente sem necessidade de dependências pesadas.
                    \"\"\"
                    import json
                    from http.server import HTTPServer, BaseHTTPRequestHandler

                    PORT = 8080

                    class NexusApiHandler(BaseHTTPRequestHandler):
                        def do_GET(self):
                            if self.path == "/" or self.path == "/status":
                                self.send_response(200)
                                self.send_header("Content-Type", "application/json")
                                self.end_headers()
                                response = {
                                    "status": "online",
                                    "server": "Nexus Terminal API Server",
                                    "platform": "Android",
                                    "message": "API respondendo com sucesso!"
                                }
                                self.wfile.write(json.dumps(response, indent=2).encode('utf-8'))
                            else:
                                self.send_response(404)
                                self.send_header("Content-Type", "application/json")
                                self.end_headers()
                                self.wfile.write(b'{"error": "Endpoint nao encontrado"}')

                        def log_message(self, format, *args):
                            print(f"[HTTP] {self.address_string()} - {format % args}")

                    def run():
                        server_address = ('', PORT)
                        httpd = HTTPServer(server_address, NexusApiHandler)
                        print(f"★ Servidor Nexus API iniciado em http://localhost:{PORT}")
                        print("➜ Acesse pelo navegador do celular ou teste com a ferramenta de rede!")
                        print("➜ Pressione CTRL+C para encerrar o servidor.")
                        try:
                            httpd.serve_forever()
                        except KeyboardInterrupt:
                            print("\n[Servidor encerrado]")
                            httpd.server_close()

                    if __name__ == '__main__':
                        run()
                """.trimIndent(),
                "requirements.txt" to "# Dependências adicionais (opcional: fastapi, uvicorn)\n",
                "README.md" to "# $projectName API\n\nServidor REST HTTP para Nexus Terminal.\n"
            )

            ProjectTemplateType.AUTOMATION_SCRIPT -> mapOf(
                "main.py" to """
                    # -*- coding: utf-8 -*-
                    \"\"\"
                    Script de Automação e Monitoramento do Sistema
                    NEXUS TERMINAL
                    \"\"\"
                    import os
                    import sys
                    import time
                    import platform

                    def get_system_summary():
                        return {
                            "system": platform.system(),
                            "node": platform.node(),
                            "release": platform.release(),
                            "machine": platform.machine(),
                            "python": sys.version.split()[0],
                            "time": time.strftime("%Y-%m-%d %H:%M:%S")
                        }

                    def main():
                        print("★ Iniciando Rotina de Automação Nexus...")
                        info = get_system_summary()
                        print("\n=== Diagnóstico do Sistema ===")
                        for k, v in info.items():
                            print(f"  • {k.capitalize()}: {v}")
                        print("\n✓ Rotina de automação concluída com sucesso.")

                    if __name__ == "__main__":
                        main()
                """.trimIndent(),
                "README.md" to "# $projectName\n\nScript de automação para tarefas periódicas.\n"
            )

            ProjectTemplateType.CLI_TOOL -> mapOf(
                "main.py" to """
                    # -*- coding: utf-8 -*-
                    \"\"\"
                    Utilitário CLI interativo para Nexus Terminal
                    \"\"\"
                    import sys

                    def banner():
                        print("\033[1;35m")
                        print("========================================")
                        print("       NEXUS CLI TOOL - $projectName    ")
                        print("========================================")
                        print("\033[0m")

                    def main():
                        banner()
                        print("\033[1;32m[✓] CLI inicializada no Android!\033[0m")
                        if len(sys.argv) > 1:
                            print(f"Argumentos recebidos: {sys.argv[1:]}")
                        else:
                            print("Dica: Você pode passar argumentos ao executar pelo terminal.")

                    if __name__ == "__main__":
                        main()
                """.trimIndent(),
                "README.md" to "# $projectName CLI\n"
            )

            ProjectTemplateType.DATA_SCIENCE -> mapOf(
                "main.py" to """
                    # -*- coding: utf-8 -*-
                    \"\"\"
                    Data Science & Estatística - Nexus Terminal
                    \"\"\"
                    import math
                    import random

                    def generate_data(n=20):
                        return [random.randint(10, 100) for _ in range(n)]

                    def calculate_metrics(data):
                        mean = sum(data) / len(data)
                        variance = sum((x - mean) ** 2 for x in data) / len(data)
                        std_dev = math.sqrt(variance)
                        return mean, std_dev

                    def main():
                        print("★ Nexus Data Science Analyzer")
                        data = generate_data()
                        print(f"Dados gerados ({len(data)} amostras): {data}")
                        mean, std = calculate_metrics(data)
                        print(f"• Média: {mean:.2f}")
                        print(f"• Desvio Padrão: {std:.2f}")
                        print(f"• Máximo: {max(data)} | Mínimo: {min(data)}")

                    if __name__ == "__main__":
                        main()
                """.trimIndent(),
                "README.md" to "# $projectName Data Science\n"
            )

            ProjectTemplateType.EMPTY -> mapOf(
                "main.py" to """
                    # -*- coding: utf-8 -*-
                    # Nexus Terminal - Projeto: $projectName

                    def main():
                        print("Olá do Nexus Terminal!")

                    if __name__ == "__main__":
                        main()
                """.trimIndent(),
                "README.md" to "# $projectName\n"
            )
        }
    }
}
