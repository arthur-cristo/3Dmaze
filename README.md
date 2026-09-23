# 3D Maze

Jogo de PC onde o jogador deve encontrar a saída de um **labirinto 3D gerado proceduralmente**.

## 🎮 Requisitos

* [ ] Tela inicial com botão `PLAY`
* [ ] Geração aleatória de labirintos
* [ ] Labirinto criado visualmente através de código
* [ ] Estilo 3D simplificado e monocromático
* [ ] Movimento com `WASD` ou setas
* [ ] Colisão com paredes
* [ ] Detecção da saída
* [ ] Vitória ao sair do labirinto
* [ ] Possibilidade de iniciar uma nova partida

---

# 📋 Etapas

## 1. Setup

* [x] Criar projeto
* [x] Configurar janela/engine
* [ ] Configurar controles (WASD, Setas, Esc)
* [x] Criar estrutura de pastas

---

## 2. Desenvolvimento

### 🧩 Gerador de labirinto

* [ ] Criar representação do mapa
* [ ] Implementar geração procedural
* [ ] Garantir entrada e saída
* [ ] Garantir que exista um caminho válido

### 🕹️ Jogador

* [ ] Criar jogador
* [ ] Implementar WASD
* [ ] Implementar setas
* [ ] Implementar colisões

### 🧱 Renderização

* [ ] Gerar paredes através de código
* [ ] Gerar chão e teto
* [ ] Configurar câmera
* [ ] Posicionar elementos de acordo com o mapa

### 🎨 Visual

* [ ] Definir estilo monocromático
* [ ] Configurar materiais e iluminação
* [ ] Ajustar câmera e aparência geral

### 🖥️ Menu

* [ ] Criar tela inicial
* [ ] Adicionar `PLAY`
* [ ] Adicionar opção de sair

---

## 3. Integração

* [ ] Conectar o gerador à renderização
* [ ] Criar um novo mapa ao iniciar uma partida
* [ ] Posicionar o jogador na entrada
* [ ] Implementar colisão com o mapa
* [ ] Criar área de saída
* [ ] Detectar quando o jogador chega à saída
* [ ] Criar tela/mensagem de vitória

---

## 4. Polimento

* [ ] Ajustar velocidade e controles
* [ ] Ajustar tamanho dos labirintos
* [ ] Melhorar iluminação e contraste
* [ ] Corrigir bugs
* [ ] Testar diferentes mapas e seeds
* [ ] Verificar performance

---

# ⭐ Extras opcionais

Após o MVP estar completo:

* [ ] Cronômetro
* [ ] Pontuação
* [ ] Diferentes dificuldades
* [ ] Minimapa
* [ ] Inimigo
* [ ] Sons e música
* [ ] Efeitos visuais
* [ ] Menu de configurações

---

## ✅ MVP

O projeto está concluído quando for possível:

**Abrir → PLAY → gerar labirinto → andar com WASD/setas → encontrar a saída → vencer → jogar novamente.**
