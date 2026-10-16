# EnderNova Launcher

Painel web em React + Vite para gerenciar versões e perfis do Minecraft Java.

## Funciona nesta etapa
- Consulta o manifesto oficial de versões da Mojang em tempo real.
- Pesquisa e filtra versões por tipo.
- Solicita ao navegador o JSON de metadados e o JAR cliente da versão escolhida.
- Cria, seleciona e remove perfis offline locais.
- Cria e remove configurações de instâncias.
- Salva perfis, instâncias, histórico e preferência de memória no armazenamento local do navegador.

## Limitações importantes
Este repositório é uma aplicação web, não um launcher Android nativo. Baixar o JAR e o JSON **não** é uma instalação completa jogável. Ainda não baixa todas as bibliotecas e assets, instala o Java Runtime, instala Forge/Fabric/OptiFine, executa a JVM no Android ou autentica contas Microsoft/Ely.by. Perfis offline não são contas oficiais e não permitem entrar em servidores que exigem autenticação. Nunca digite senhas neste painel.

Para jogar no Android será necessário um componente nativo compatível com o runtime Java, gerenciamento completo de bibliotecas/assets, integração de autenticação oficial e testes no dispositivo.

## Desenvolvimento
Requer Node.js 20.19+ ou 22.12+.

```sh
npm install
npm run dev
npm run build
```

O catálogo e os downloads exigem internet e podem depender das permissões do navegador.
