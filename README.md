# 3D Maze

Jogo de PC onde o jogador deve encontrar a saída de um **labirinto 3D gerado proceduralmente**.

## 🎮 Requisitos

* [x] Tela inicial com botão `PLAY`
* [x] Geração aleatória de labirintos
* [x] Labirinto criado visualmente através de código
* [x] Estilo 3D simplificado e monocromático
* [x] Movimento com `WASD` ou setas
* [x] Colisão com paredes
* [x] Detecção da saída
* [x] Vitória ao sair do labirinto
* [x] Possibilidade de iniciar uma nova partida

---

# 📋 Etapas

## 1. Setup

* [x] Criar projeto
* [x] Configurar janela/engine
* [x] Configurar controles (WASD, Setas, Esc)
* [x] Criar estrutura de pastas

---

## 2. Desenvolvimento

### 🧩 Gerador de labirinto

* [x] Criar representação do mapa
* [x] Implementar geração procedural
* [x] Garantir entrada e saída
* [x] Garantir que exista um caminho válido

### 🕹️ Jogador

* [x] Criar jogador
* [x] Implementar WASD
* [x] Implementar setas
* [x] Implementar colisões

### 🧱 Renderização

* [x] Gerar paredes através de código
* [x] Gerar chão e teto
* [x] Configurar câmera
* [x] Posicionar elementos de acordo com o mapa

### 🎨 Visual

* [x] Definir estilo monocromático
* [x] Configurar materiais e iluminação
* [x] Ajustar câmera e aparência geral

### 🖥️ Menu

* [x] Criar tela inicial
* [x] Adicionar `PLAY`
* [x] Adicionar opção de sair

---

## 3. Integração

* [x] Conectar o gerador à renderização
* [x] Criar um novo mapa ao iniciar uma partida
* [x] Posicionar o jogador na entrada
* [x] Implementar colisão com o mapa
* [x] Criar área de saída
* [x] Detectar quando o jogador chega à saída
* [x] Criar tela/mensagem de vitória

---

## 4. Polimento

* [x] Ajustar velocidade e controles
* [x] Ajustar tamanho dos labirintos
* [x] Melhorar iluminação e contraste
* [] Corrigir bugs
* [] Testar diferentes mapas e seeds
* [] Verificar performance

---

# ⭐ Extras opcionais

Após o MVP estar completo:

* [x] Cronômetro
* [x] Pontuação
* [] Diferentes dificuldades
* [] Minimapa
* [] Inimigo
* [] Sons e música
* [] Efeitos visuais
* [x] Menu de configurações

---

## ✅ MVP

O projeto está concluído quando for possível:

**Abrir → PLAY → gerar labirinto → andar com WASD/setas → encontrar a saída → vencer → jogar novamente.**
