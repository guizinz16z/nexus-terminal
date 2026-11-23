# 🚀 NEXUS TERMINAL — Android Mobile Development & Linux Station

<p align="center">
  <img src="app/src/main/res/drawable/ic_nexus_logo.xml" width="120" height="120" alt="Nexus Terminal Logo" />
</p>

<p align="center">
  <b>A fusão definitiva entre Termux, Pydroid 3, IDE de Programação e Terminal Linux.</b><br>
  Desenvolvido com arquitetura moderna Kotlin, Material Design 3 e estética Cyber Dark.
</p>

---

## 🌟 Recursos Principais

### 1. 💻 Terminal Linux Interativo
- **Shell Real:** Suporte a `/bin/bash`, `/bin/sh` e shell Android com fallback inteligente.
- **PTY & Multi-Sessão:** Abas simultâneas independentes para executar múltiplos processos.
- **Parser ANSI 256 Cores:** Cores fiéis para comandos como `ls --color`, `htop`, `neofetch` e logs.
- **Barra de Teclas Especiais:** Acesso rápido a `ESC`, `TAB`, `CTRL`, `ALT`, setas `▲ ▼ ◀ ▶`, `|`, `/`, `-`.
- **Controle de Processos:** Envio de `SIGINT` (Ctrl+C), limpeza de buffer e histórico de comandos.

### 2. 🐍 Ambiente Python Completo
- **Runtime Python 3.11+:** Execução de scripts `.py` e avaliação de código interativo.
- **Streaming stdout/stderr em tempo real:** Saída sem buffer (`PYTHONUNBUFFERED=1`) com contador de tempo de execução.
- **Gerenciador PIP Dedicado:** Interface gráfica para pesquisar bibliotecas no **PyPI**, instalar diretamente no ambiente do app (`site-packages`), atualizar e desinstalar.
- **Ambiente Isolado:** Manipulação automática de `PYTHONPATH` para carregar módulos do usuário e do projeto.

### 3. 📝 Editor de Código Profissional
- **Syntax Highlighting:** Realce de sintaxe em tempo real para Python, Shell Script, JSON, Markdown, HTML, CSS e JavaScript.
- **Numeração de Linhas Dinâmica:** Gutter sincronizado com rolagem vertical.
- **Barra de Símbolos de Programação:** Inserção imediata com 1 toque de `TAB`, `:`, `()`, `[]`, `{}`, `=`, `"`, `#`, `.`.
- **Gaveta de Execução Integrada:** Execute seu script com 1 toque e veja os resultados sem sair do editor.
- **Integração com IA Copilot:** Envio direto do código para análise, explicação e correção.

### 4. 📁 Gerenciador de Arquivos & Projetos
- **Explorador Visual:** Navegação rápida por `/workspace`, armazenamento interno e `/sdcard`.
- **Operações Completas:** Criar arquivos, criar pastas, renomear, excluir com confirmação, ler e editar.
- **Templates de Projetos Prontos:**
  - 🤖 **Telegram Bot:** Template de automação com pooling assíncrono e comandos.
  - 🌐 **API Web Server:** Servidor HTTP REST leve em Python pronto para rodar.
  - ⚡ **Script de Automação:** Diagnóstico de sistema, consumo de RAM e logs.
  - 🖥️ **Ferramenta CLI:** Utilitário interativo com banner ANSI e argumentos.
  - 📊 **Data Science & Math:** Processamento estatístico de dados e amostras.
  - 📄 **Projeto Vazio:** Estrutura limpa `main.py` + `README.md`.

### 5. 🌿 Git & Ferramentas de Sistema
- **Git VCS:** Clone repositórios do GitHub/GitLab, visualize arquivos modificados, realize commits com mensagens, execute `pull` e `push`.
- **Central de Diagnóstico:** Ping de rede, teste de requisições HTTP (alternativa cURL), monitor de processos com encerramento por PID.
- **Criptografia & Hashes:** Cálculo de MD5, SHA-256 e codificação/decodificação Base64 e URL.
- **Gerador de Chaves SSH:** Criação de pares de chaves RSA de 2048-bit para autenticação com GitHub.

### 6. 🤖 Assistente de Código com IA
- **Suporte Multi-Provedor:** Suporta chaves de API da **OpenAI (ChatGPT)**, **Google Gemini** ou **OpenRouter**.
- **Privacidade Total:** Suas chaves de API são armazenadas apenas no seu dispositivo Android via SharedPreferences criptografadas.
- **Chips Rápidos:** "Explicar Código", "Corrigir Erro", "Otimizar Script".

### 7. 💾 Backup & Restauração
- Exportação em 1 toque de todos os seus projetos e scripts para um arquivo ZIP (`NexusTerminal_Backup_YYYYMMDD.zip`).

---

## 🛠️ Como Compilar o APK pelo Celular (GitHub Actions)

Você **NÃO precisa de um computador** para gerar o APK instalável do Nexus Terminal. O projeto já vem configurado com **GitHub Actions CI/CD**.

### Passo a Passo no Celular:

1. **Suba o projeto para o GitHub:**
   ```bash
   cd /workspace/nexus-terminal
   git init
   git add .
   git commit -m "feat: initial commit of Nexus Terminal Android app"
   git branch -M main
   git remote add origin https://github.com/SEU_USUARIO/nexus-terminal.git
   git push -u origin main
   ```

2. **Acompanhe a Compilação:**
   - Abra o GitHub no navegador do celular ou no app do GitHub.
   - Acesse o repositório e toque na aba **Actions**.
   - O workflow **"Build Nexus Terminal APK"** iniciará automaticamente.

3. **Baixe e Instale o APK:**
   - Quando a compilação finalizar (ícone verde de check):
   - Toque na execução concluída.
   - Na seção **Artifacts**, baixe:
     - `NexusTerminal-Release-APK` (ou `NexusTerminal-Debug-APK`).
   - Extraia o ZIP baixado no celular e toque no arquivo `.apk` para instalar!

---

## 📐 Estrutura do Repositório

```text
nexus-terminal/
├── .github/
│   └── workflows/
│       └── build-apk.yml          # Automação de compilação do APK
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # Permissões, intents e configuração
│   │   ├── java/com/nexus/terminal/
│   │   │   ├── NexusApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── core/             # Motores de Terminal, Python, PIP, Editor, Git, AI
│   │   │   ├── data/             # Preferências e modelos de dados
│   │   │   └── ui/               # Dashboard, Terminal, Editor, Files, Packages, Tools, etc.
│   │   └── res/                  # Layouts, estilos glassmorphism, drawables e cores
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 📄 Licença
Distribuído sob a licença MIT. Criado com maestria com Google Antigravity & AndCode.
