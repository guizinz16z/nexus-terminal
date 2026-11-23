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
echo "Instruções:"
echo "1. Crie um repositório vazio no GitHub: https://github.com/new"
echo "   Nome sugerido: nexus-terminal (marque como Public para GitHub Actions ilimitado)"
echo "2. Gere um Token de Acesso Pessoal (PAT) com permissão 'repo':"
echo "   https://github.com/settings/tokens/new"
echo ""

read -p "Digite seu usuário do GitHub: " GH_USER
read -p "Digite o nome do repositório (padrão: nexus-terminal): " GH_REPO
GH_REPO=${GH_REPO:-nexus-terminal}

read -s -p "Digite ou cole seu Token do GitHub (PAT): " GH_TOKEN
echo ""

if [ -z "$GH_USER" ] || [ -z "$GH_TOKEN" ]; then
    echo "❌ Usuário ou Token não podem ser vazios."
    exit 1
fi

REMOTE_URL="https://${GH_USER}:${GH_TOKEN}@github.com/${GH_USER}/${GH_REPO}.git"

# Remove existing origin if present
git remote remove origin 2>/dev/null || true
git remote add origin "$REMOTE_URL"

echo "Enviando branch 'main' para o GitHub..."
if git push -u origin main; then
    echo ""
    echo "======================================================"
    echo "  🎉 SUCESSO! Código enviado com sucesso para o GitHub!"
    echo "======================================================"
    echo ""
    echo "Agora acesse no navegador do celular:"
    echo "👉 https://github.com/${GH_USER}/${GH_REPO}/actions"
    echo ""
    echo "O GitHub Actions já está compilando o seu APK (Debug & Release)!"
    echo "Quando finalizar, faça o download do artefato 'NexusTerminal-Release-APK'."
else
    echo ""
    echo "❌ Ocorreu um erro no git push."
    echo "Dica: Verifique se o repositório existe e se o Token possui a permissão 'repo'."
fi
