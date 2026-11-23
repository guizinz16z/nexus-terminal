#!/bin/bash
set -e

echo "======================================================"
echo "    🚀 NEXUS TERMINAL - Publicador GitHub (Mobile)    "
echo "======================================================"
echo ""

cd /workspace/nexus-terminal

echo "Verificando repositório Git local..."
if [ ! -d ".git" ]; then
    echo "❌ Erro: Diretório .git não encontrado em /workspace/nexus-terminal"
    exit 1
fi

echo "Status local:"
git log -n 1 --oneline
echo ""

GH_USER="$1"
GH_TOKEN="$2"
GH_REPO="${3:-nexus-terminal}"

if [ -z "$GH_USER" ]; then
    if [ -t 0 ]; then
        read -p "Digite seu usuário do GitHub: " GH_USER
        read -p "Digite o nome do repositório (padrão: nexus-terminal): " INPUT_REPO
        GH_REPO=${INPUT_REPO:-nexus-terminal}
        read -s -p "Digite ou cole seu Token do GitHub (PAT): " GH_TOKEN
        echo ""
    fi
fi

if [ -z "$GH_USER" ] || [ -z "$GH_TOKEN" ]; then
    echo "❌ Usuário ou Token não informados."
    echo ""
    echo "📌 Como usar diretamente:"
    echo "   ./push_to_github.sh <seu_usuario> <seu_token_pat> [nome_repositorio]"
    echo ""
    echo "1. Crie o repositório no GitHub: https://github.com/new"
    echo "   (Nome: nexus-terminal | Marque: Public)"
    echo "2. Gere seu Token: https://github.com/settings/tokens/new"
    echo "   (Marque a caixinha: repo)"
    exit 1
fi

REMOTE_URL="https://${GH_USER}:${GH_TOKEN}@github.com/${GH_USER}/${GH_REPO}.git"

# Remove existing origin if present
git remote remove origin 2>/dev/null || true
git remote add origin "$REMOTE_URL"

echo "Enviando branch 'main' para o GitHub (${GH_USER}/${GH_REPO})..."
if git push -u origin main; then
    echo ""
    echo "======================================================"
    echo "  🎉 SUCESSO! Código enviado com sucesso para o GitHub!"
    echo "======================================================"
    echo ""
    echo "Acesse a compilação do seu APK no navegador:"
    echo "👉 https://github.com/${GH_USER}/${GH_REPO}/actions"
    echo ""
    echo "O GitHub Actions está compilando o Nexus Terminal APK!"
    echo "Em cerca de 2 minutos, baixe o artefato 'NexusTerminal-Release-APK' para instalar no celular."
else
    echo ""
    echo "❌ Ocorreu um erro no git push."
    echo "Dica: Verifique se o repositório https://github.com/${GH_USER}/${GH_REPO} foi criado e se o token tem permissão 'repo'."
fi
