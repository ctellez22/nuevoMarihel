#!/bin/zsh
# Script para instalar CUPS (sistema de impresión) en macOS

echo "================================"
echo "Instalador de CUPS para macOS"
echo "================================"
echo ""

# Verificar si Homebrew está instalado
if ! command -v brew &> /dev/null; then
    echo "❌ Homebrew no está instalado."
    echo "Instálalo primero desde: https://brew.sh"
    echo ""
    echo "En terminal, ejecuta:"
    echo '/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"'
    exit 1
fi

echo "✅ Homebrew encontrado"
echo ""

# Verificar si CUPS ya está instalado
if command -v lp &> /dev/null; then
    echo "✅ CUPS ya está instalado en tu sistema"
    echo ""
    echo "Para verificar las impresoras disponibles, ejecuta:"
    echo "    lpstat -p -d"
    echo ""
    exit 0
fi

echo "📦 Instalando CUPS..."
brew install cups

echo ""
echo "================================"
echo "Instalación completada"
echo "================================"
echo ""

# Mostrar próximos pasos
echo "📋 Próximos pasos:"
echo ""
echo "1. Conecta tu impresora Zebra ZD230 a la red"
echo ""
echo "2. Añade la impresora a macOS:"
echo "    open /Applications/Utilities/Printer*\ Setup\ Assistant.app"
echo "    O: Sistema > Impresoras y escáneres > Agregar"
echo ""
echo "3. Verifica que la impresora está disponible:"
echo "    lpstat -p -d"
echo ""
echo "4. Ejecuta la aplicación Marihel:"
echo "    ./scripts/run-local-macos.sh"
echo ""
echo "5. Si la impresora tiene un nombre diferente, usa:"
echo "    java -Dprinter.name=\"TU_NOMBRE_EXACTO\" -jar marihel.jar"
echo ""

