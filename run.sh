#!/bin/bash

echo "==================================="
echo "Feature Flag NOP - Build & Run"
echo "==================================="
echo ""

# Cores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Limpar builds anteriores
echo -e "${BLUE}1. Limpando builds anteriores...${NC}"
mvn clean

# Compilar
echo -e "${BLUE}2. Compilando projeto...${NC}"
mvn package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Falha na compilação!${NC}"
    exit 1
fi

# Menu
echo ""
echo -e "${GREEN}✅ Compilação bem-sucedida!${NC}"
echo ""
echo "O que você gostaria de executar?"
echo "1. Demonstração (Main)"
echo "2. Benchmark JMH (completo)"
echo "3. Benchmark JMH (rápido)"
echo ""
read -p "Escolha uma opção (1-3): " choice

case $choice in
    1)
        echo -e "${YELLOW}Executando demonstração...${NC}"
        java -cp target/feature-flag-nop-1.0.jar com.example.Main
        ;;
    2)
        echo -e "${YELLOW}Executando benchmark completo (pode demorar alguns minutos)...${NC}"
        java -jar target/feature-flag-nop-1.0.jar
        ;;
    3)
        echo -e "${YELLOW}Executando benchmark rápido...${NC}"
        java -jar target/feature-flag-nop-1.0.jar -wi 1 -i 2 -f 1
        ;;
    *)
        echo "Opção inválida!"
        exit 1
        ;;
esac
